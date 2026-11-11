
package com.krmob.screentextreader.feature.capture.ui

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krmob.screentextreader.core.coroutines.DefaultDispatcher
import com.krmob.screentextreader.feature.analysis.domain.AnalysisSettings
import com.krmob.screentextreader.feature.analysis.domain.CrashMultiplierParser
import com.krmob.screentextreader.feature.analysis.domain.DecimalSeparator
import com.krmob.screentextreader.feature.analysis.domain.TextAnalyzer
import com.krmob.screentextreader.feature.capture.domain.CaptureSession
import com.krmob.screentextreader.feature.capture.domain.ScreenCaptureController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

private const val STATE_SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L
private const val KEY_MINIMUM_CONFIDENCE = "minimumConfidence"
private const val KEY_DECIMAL_SEPARATOR = "decimalSeparator"

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
