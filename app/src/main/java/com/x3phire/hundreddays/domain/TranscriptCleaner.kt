package com.x3phire.hundreddays.domain

import java.util.Locale

interface TranscriptCleaner {
    suspend fun cleanTranscript(rawTranscript: String): String
}

/**
 * Rule-based transcript cleaner and bullet formatter.
 * Cleans speech fillers (um, uh, you know, etc.), breaks spoken speech into
 * coherent thoughts, capitalizes sentences, and formats as markdown bullets.
 */
class RuleBasedTranscriptCleaner : TranscriptCleaner {

    private val fillerWordsRegex = Regex(
        "(?i)\\b(um+|uh+|er+|ah+|you know|basically|actually|sort of|kind of|i mean)\\b[,.]?",
    )

    private val thoughtSplitterRegex = Regex(
        "(?i)(?:\\r?\\n+|[.!?]+\\s+|\\s+(?:and then|and also|after that|next|also)\\s+)",
    )

    private val leadingConjunctionsRegex = Regex(
        "(?i)^\\s*(?:and|so|but|then|also|well)\\s+",
    )

    override suspend fun cleanTranscript(rawTranscript: String): String {
        val trimmed = rawTranscript.trim()
        if (trimmed.isEmpty()) return ""

        // Check if the input is already formatted as multiple bulleted lines
        val existingLines = trimmed.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val isAlreadyBulleted = existingLines.size > 1 && existingLines.all { line ->
            line.startsWith("•") || line.startsWith("-") || line.startsWith("*") || line.matches(Regex("^\\d+\\..*"))
        }

        if (isAlreadyBulleted) {
            return existingLines.mapNotNull { line ->
                val content = line.replaceFirst(Regex("^([•\\-*]|\\d+\\.)\\s*"), "")
                val cleanedContent = cleanSingleThought(content)
                if (cleanedContent.isNotBlank()) "• $cleanedContent" else null
            }.joinToString("\n")
        }

        // Clean filler words from the entire text
        val cleaned = fillerWordsRegex.replace(trimmed, " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()

        if (cleaned.isEmpty()) return ""

        // Split into thought segments
        val segments = cleaned.split(thoughtSplitterRegex)
            .map { cleanSingleThought(it) }
            .filter { it.isNotBlank() }

        return if (segments.isEmpty()) {
            "• ${cleanSingleThought(cleaned)}"
        } else {
            segments.joinToString("\n") { "• $it" }
        }
    }

    private fun cleanSingleThought(raw: String): String {
        var text = fillerWordsRegex.replace(raw.trim(), " ")
            .replace(Regex("\\s{2,}"), " ")
            .trim()
        // Remove leading punctuation or conjunctions
        text = text.replaceFirst(Regex("^[•\\-*,;.]+\\s*"), "")
        text = leadingConjunctionsRegex.replace(text, "")
        // Remove trailing punctuation
        text = text.replace(Regex("[,;.]+$"), "").trim()
        if (text.isEmpty()) return ""

        // Capitalize first letter
        val firstChar = text.first().uppercase(Locale.getDefault())
        return firstChar + text.substring(1)
    }
}
