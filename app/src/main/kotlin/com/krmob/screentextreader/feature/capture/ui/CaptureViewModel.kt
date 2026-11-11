
package com.krmob.screentextreader.feature.capture.ui

import android.content.Intent
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krmob.screentextreader.core.coroutines.DefaultDispatcher
import com.krmob.screentextreader.feature.analysis.domain.AnalysisSettings
import com.krmob.screentextreader.feature.analysis.domain.CrashMultiplierParser
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.analysis.domain.TextAnalyzer
import com.krmob.screentextreader.feature.capture.domain.CaptureSession
import com.krmob.screentextreader.feature.capture.domain.CaptureState
import com.krmob.screentextreader.feature.capture.domain.ScreenCaptureController
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private const val STATE_SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
private const val KEY_MINIMUM_CONFIDENCE = "minimumConfidence"
private const val KEY_DECIMAL_SEPARATOR = "decimalSeparator"
private const val WORKER_URL =
    "https://crash-collector-final.franceagrimer-fr.workers.dev/"
private const val TAG = "CrashMultiplierSender"

@HiltViewModel
class CaptureViewModel @Inject constructor(
    captureSession: CaptureSession,
    private val captureController: ScreenCaptureController,
    private val textAnalyzer: TextAnalyzer,
    private val crashMultiplierParser: CrashMultiplierParser,
    private val savedStateHandle: SavedStateHandle,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val settings: Flow<AnalysisSettings> = combine(
        savedStateHandle.getStateFlow(KEY_MINIMUM_CONFIDENCE, AnalysisSettings.DEFAULT.minimumConfidence),
        savedStateHandle.getStateFlow(KEY_DECIMAL_SEPARATOR, AnalysisSettings.DEFAULT.decimalSeparator)
    ) { minimumConfidence, decimalSeparator ->
        AnalysisSettings(minimumConfidence = minimumConfidence, decimalSeparator = decimalSeparator)
    }

    val uiState: StateFlow<CaptureUiState> = combine(
        captureSession.state,
        captureSession.recognizedText,
        settings
    ) { captureState, recognizedText, settings ->
        val analysis = textAnalyzer.analyze(recognizedText, settings)
        val multipliers = crashMultiplierParser.parse(
            analysis.acceptedLines.map { it.text }
        )

        CaptureUiState(
            captureState = captureState,
            settings = settings,
            acceptedLines = analysis.acceptedLines.toImmutableList(),
            rejectedLineCount = analysis.rejectedLineCount,
            statistics = analysis.statistics,
            crashMultipliers = multipliers.toImmutableList()
        )
    }
        .flowOn(defaultDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = CaptureUiState.INITIAL
        )

    init {
        observeMultipliersForUpload()
    }

    private fun observeMultipliersForUpload() {
        viewModelScope.launch(Dispatchers.IO) {
            var previousMultipliers = emptyList<BigDecimal>()

            uiState.collect { state ->
                val isCapturing = state.captureState is CaptureState.Running &&
                    !state.captureState.isPaused

                if (!isCapturing) return@collect

                val currentMultipliers = state.crashMultipliers.toList()

                if (currentMultipliers.isEmpty() || currentMultipliers == previousMultipliers) {
                    return@collect
                }

                val newMultipliers = findNewMultipliers(
                    previous = previousMultipliers,
                    current = currentMultipliers
                )

                previousMultipliers = currentMultipliers

                if (newMultipliers.isNotEmpty()) {
                    sendMultipliers(newMultipliers)
                }
            }
        }
    }

    private fun findNewMultipliers(previous: List<BigDecimal>, current: List<BigDecimal>): List<BigDecimal> {
        if (previous.isEmpty()) return current

        val maxOverlap = minOf(previous.size, current.size)

        for (overlap in maxOverlap downTo 1) {
            if (previous.takeLast(overlap) == current.take(overlap)) {
                return current.drop(overlap)
            }

            if (previous.take(overlap) == current.takeLast(overlap)) {
                return current.dropLast(overlap)
            }
        }

        return current
    }

    private fun sendMultipliers(multipliers: List<BigDecimal>) {
        val connection = URL(WORKER_URL).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val payload = JSONObject()
                .put(
                    "multipliers",
                    JSONArray(multipliers.map { it.toPlainString() })
                )
                .toString()

            connection.outputStream.use { output ->
                output.write(payload.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode

            if (responseCode in 200..299) {
                Log.d(TAG, "Multiplicateurs envoyés : $multipliers")
            } else {
                Log.e(TAG, "Erreur Worker HTTP : $responseCode")
            }
        } catch (exception: Exception) {
            Log.e(TAG, "Échec de l'envoi des multiplicateurs", exception)
        } finally {
            connection.disconnect()
        }
    }

    fun createConsentIntent(): Intent = captureController.createConsentIntent()

    fun onConsentResult(resultCode: Int, data: Intent?) {
        captureController.onConsentResult(resultCode, data)
    }

    fun onStopClick() {
        captureController.stop()
    }

    fun onFailureDismiss() {
        captureController.dismissFailure()
    }

    fun onMinimumConfidenceChange(minimumConfidence: Float) {
        savedStateHandle[KEY_MINIMUM_CONFIDENCE] =
            minimumConfidence.coerceIn(AnalysisSettings.CONFIDENCE_RANGE)
    }

    fun onDecimalSeparatorChange(decimalSeparator: DecimalSeparator) {
        savedStateHandle[KEY_DECIMAL_SEPARATOR] = decimalSeparator
    }
}
