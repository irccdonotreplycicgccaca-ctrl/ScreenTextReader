package com.krmob.screentextreader.feature.capture.domain

import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import kotlin.time.Duration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

/** Recognizes text on the most recent captured frame. */
fun interface ScreenTextReader {
    /** Returns null when nothing new has been captured since the previous call. */
    suspend fun readLatestFrame(): RecognizedText?
}

/**
 * Reads the screen every [interval] while [shouldScan] is true and emits only text that differs from the previous
 * emission. Frames are pulled one at a time, so a slow recognizer delays the next read instead of queueing frames.
 * A pause stops new reads but lets a read in progress finish, because its frame was captured before the pause.
 */
fun ScreenTextReader.scan(shouldScan: Flow<Boolean>, interval: Duration): Flow<RecognizedText> = channelFlow {
    val isScanning = shouldScan.stateIn(this)
    while (true) {
        isScanning.first { scanning -> scanning }
        readLatestFrame()?.let { recognizedText -> send(recognizedText) }
        delay(interval)
    }
}.distinctUntilChangedBy { recognizedText -> recognizedText.lines.map(RecognizedLine::text) }
