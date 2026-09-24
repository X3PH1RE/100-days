package com.x3phire.hundreddays.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TranscriptCleanerTest {

    private val cleaner = RuleBasedTranscriptCleaner()

    @Test
    fun emptyOrBlankInputReturnsEmptyString() = runTest {
        assertEquals("", cleaner.cleanTranscript(""))
        assertEquals("", cleaner.cleanTranscript("   \n\t  "))
    }

    @Test
    fun stripsFillerWordsAndFormatsSingleThought() = runTest {
        val input = "um i finished the android widget design today uh you know"
        val result = cleaner.cleanTranscript(input)
        assertEquals("• I finished the android widget design today", result)
    }

    @Test
    fun splitsMultipleThoughtsIntoCleanBullets() = runTest {
        val input = "um today i built the speech recognizer and then i fixed the matrix border and also added unit tests"
        val result = cleaner.cleanTranscript(input)
        val expected = """
            • Today i built the speech recognizer
            • I fixed the matrix border
            • Added unit tests
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun handlesPunctuatedSentences() = runTest {
        val input = "Finished work on the project. Read twenty pages of my book! Drank 2L water."
        val result = cleaner.cleanTranscript(input)
        val expected = """
            • Finished work on the project
            • Read twenty pages of my book
            • Drank 2L water
        """.trimIndent()
        assertEquals(expected, result)
    }

    @Test
    fun normalizesExistingBullets() = runTest {
        val input = """
            - um first task
            * uh second task
            • third task
        """.trimIndent()
        val result = cleaner.cleanTranscript(input)
        val expected = """
            • First task
            • Second task
            • Third task
        """.trimIndent()
        assertEquals(expected, result)
    }
}
