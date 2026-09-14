package com.krmob.screentextreader.feature.capture.domain

import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import kotlin.time.Duration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

/** Recognizes text on the most recent captured frame. */
fun interface ScreenTextReader {
    /** Returns null when nothing new has been captured since the previous call. */
    suspend fun readLatestFrame(): RecognizedText?
}

/**
 * Reads the screen every [interval] while [shouldScan] emits true, and emits only text that differs from the
 * previous emission. Frames are pulled one at a time, so a slow recognizer delays the next read instead of
 * letting frames queue up.
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun ScreenTextReader.scan(shouldScan: Flow<Boolean>, interval: Duration): Flow<RecognizedText> = shouldScan
    .distinctUntilChanged()
    .flatMapLatest { isScanning -> if (isScanning) readEvery(interval) else emptyFlow() }
    .distinctUntilChangedBy { recognizedText -> recognizedText.lines.map(RecognizedLine::text) }

private fun ScreenTextReader.readEvery(interval: Duration): Flow<RecognizedText> = flow {
    while (true) {
        readLatestFrame()?.let { recognizedText -> emit(recognizedText) }
        delay(interval)
    }
}
