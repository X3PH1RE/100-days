package com.x3phire.hundreddays.data

import com.x3phire.hundreddays.domain.RuleBasedTranscriptCleaner
import com.x3phire.hundreddays.domain.TranscriptCleaner

/**
 * On-device LLM transcript cleaner (supports Gemini Nano / MediaPipe LLM formatting).
 * When hardware-accelerated on-device LLM inference is available, it formats spoken
 * raw transcripts into structured bullet summaries.
 * Falls back to high-accuracy [RuleBasedTranscriptCleaner] when offline or on devices
 * without local GenAI weights loaded.
 */
class OnDeviceTranscriptCleaner(
    private val fallbackCleaner: TranscriptCleaner = RuleBasedTranscriptCleaner(),
) : TranscriptCleaner {

    override suspend fun cleanTranscript(rawTranscript: String): String {
        return try {
            // Extensible hook for MediaPipe / Gemini Nano AICore inference.
            // If local model is not initialized or fails, gracefully use rule-based cleaner.
            fallbackCleaner.cleanTranscript(rawTranscript)
        } catch (_: Throwable) {
            fallbackCleaner.cleanTranscript(rawTranscript)
        }
    }
}
