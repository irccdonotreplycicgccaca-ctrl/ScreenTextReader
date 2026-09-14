package com.krmob.screentextreader.feature.capture.domain

import app.cash.turbine.test
import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

private val SCAN_INTERVAL = 1.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenTextScanningTest {
    @Test
    fun `emits the text of each new frame while scanning`() = runTest {
        val reader = FakeScreenTextReader(textOf("Pierwsza"), textOf("Druga"))

        reader.scan(flowOf(true), SCAN_INTERVAL).test {
            assertEquals(textOf("Pierwsza"), awaitItem())
            assertEquals(textOf("Druga"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `waits for the next interval when no new frame was captured`() = runTest {
        val reader = FakeScreenTextReader(null, null, textOf("Tekst"))

        reader.scan(flowOf(true), SCAN_INTERVAL).test {
            assertEquals(textOf("Tekst"), awaitItem())
            assertEquals(3, reader.readCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit text identical to the previous frame even when confidence differs`() = runTest {
        val reader =
            FakeScreenTextReader(
                textOf("Suma 10,00", confidence = 0.8f),
                textOf("Suma 10,00", confidence = 0.95f),
                textOf("Suma 12,00")
            )

        reader.scan(flowOf(true), SCAN_INTERVAL).test {
            assertEquals(textOf("Suma 10,00", confidence = 0.8f), awaitItem())
            assertEquals(textOf("Suma 12,00"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stops reading while paused and resumes afterwards`() = runTest {
        val reader = FakeScreenTextReader(textOf("Przed pauzą"), textOf("Po pauzie"))
        val shouldScan = MutableStateFlow(false)

        reader.scan(shouldScan, SCAN_INTERVAL).test {
            advanceTimeBy(SCAN_INTERVAL * 3)
            assertEquals(0, reader.readCount)

            shouldScan.value = true
            assertEquals(textOf("Przed pauzą"), awaitItem())

            shouldScan.value = false
            advanceTimeBy(SCAN_INTERVAL * 3)
            assertEquals(1, reader.readCount)

            shouldScan.value = true
            assertEquals(textOf("Po pauzie"), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun textOf(text: String, confidence: Float = 0.9f) =
        RecognizedText(lines = listOf(RecognizedLine(text = text, confidence = confidence)))
}

private class FakeScreenTextReader(vararg frames: RecognizedText?) : ScreenTextReader {
    private val remainingFrames = ArrayDeque(frames.toList())

    var readCount = 0
        private set

    override suspend fun readLatestFrame(): RecognizedText? {
        readCount++
        return remainingFrames.removeFirstOrNull()
    }
}
