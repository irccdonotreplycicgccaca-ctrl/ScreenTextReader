package com.krmob.screentextreader.feature.capture.data

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.krmob.screentextreader.core.model.RecognizedLine
import com.krmob.screentextreader.core.model.RecognizedText
import kotlinx.coroutines.tasks.await

// Frames mirrored from the display are already in the orientation the user sees.
private const val UPRIGHT_ROTATION_DEGREES = 0

/** Recognizes Latin script with the on-device ML Kit model bundled in the app, so it works offline. */
internal class MlKitTextRecognizer : AutoCloseable {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun recognize(frame: Bitmap): RecognizedText {
        val text = recognizer.process(InputImage.fromBitmap(frame, UPRIGHT_ROTATION_DEGREES)).await()
        return RecognizedText(
            lines =
                text.textBlocks
                    .flatMap { block -> block.lines }
                    .map { line -> RecognizedLine(text = line.text, confidence = line.confidence) }
        )
    }

    override fun close() {
        recognizer.close()
    }
}
