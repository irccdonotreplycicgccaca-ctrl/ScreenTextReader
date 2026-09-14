package com.krmob.screentextreader.core.model

/** Text recognized on one captured frame, line by line in reading order. */
data class RecognizedText(val lines: List<RecognizedLine>) {
    companion object {
        val EMPTY = RecognizedText(lines = emptyList())
    }
}

/** A single recognized line and the recognizer's confidence in it, from 0 to 1. */
data class RecognizedLine(val text: String, val confidence: Float)
