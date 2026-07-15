package com.daphle.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DictionaryApiClientTest {

    // -------------------------------------------------------
    // 1. Picks meaning with most definitions per POS
    // -------------------------------------------------------
    @Test
    fun `picks meaning with most definitions for same part of speech`() {
        // "bat": animal noun has 2 defs, sports noun has 3 defs -> sports wins
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "noun",
                    "definitions": [
                        {"definition": "A flying mammal of the order Chiroptera."},
                        {"definition": "An old woman."}
                    ]
                }
            ]
        }, {
            "meanings": [
                {
                    "partOfSpeech": "noun",
                    "definitions": [
                        {"definition": "A club made of wood used for striking a ball in sports."},
                        {"definition": "A turn at hitting the ball."},
                        {"definition": "A sheet of cotton for quilts."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(1, result.size)
        assertEquals("noun", result[0].partOfSpeech)
        assertEquals("A club made of wood used for striking a ball in sports.", result[0].definition)
    }

    // -------------------------------------------------------
    // 2. Groups across multiple top-level entries by POS
    // -------------------------------------------------------
    @Test
    fun `collects different parts of speech across entries`() {
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "adjective",
                    "definitions": [
                        {"definition": "Moving or able to move quickly."},
                        {"definition": "Firmly fixed or attached."}
                    ]
                }
            ]
        }, {
            "meanings": [
                {
                    "partOfSpeech": "noun",
                    "definitions": [
                        {"definition": "The act of abstaining from food."}
                    ]
                },
                {
                    "partOfSpeech": "verb",
                    "definitions": [
                        {"definition": "To abstain from food or drink."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(3, result.size)
        val byPos = result.associateBy { it.partOfSpeech }
        assertEquals("Moving or able to move quickly.", byPos["adjective"]?.definition)
        assertEquals("The act of abstaining from food.", byPos["noun"]?.definition)
        assertEquals("To abstain from food or drink.", byPos["verb"]?.definition)
    }

    // -------------------------------------------------------
    // 3. Skips circular/too-short definitions
    // -------------------------------------------------------
    @Test
    fun `skips definitions shorter than 15 characters`() {
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "verb",
                    "definitions": [
                        {"definition": "To run."},
                        {"definition": "To move swiftly on foot so that both feet leave the ground."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(1, result.size)
        assertEquals("To move swiftly on foot so that both feet leave the ground.", result[0].definition)
    }

    @Test
    fun `skips definitions that just restate the word`() {
        val json = """[{
            "word": "run",
            "meanings": [
                {
                    "partOfSpeech": "verb",
                    "definitions": [
                        {"definition": "To run."},
                        {"definition": "To move swiftly on foot so that both feet leave the ground."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(1, result.size)
        assertEquals("To move swiftly on foot so that both feet leave the ground.", result[0].definition)
    }

    @Test
    fun `returns empty when all definitions are too short`() {
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "noun",
                    "definitions": [
                        {"definition": "A person:"},
                        {"definition": "A thing."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(0, result.size)
    }

    // -------------------------------------------------------
    // 4. Includes example when present
    // -------------------------------------------------------
    @Test
    fun `includes example when present on chosen definition`() {
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "verb",
                    "definitions": [
                        {"definition": "To strike or hit with a bat or similar instrument.", "example": "The cat batted at the toy."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(1, result.size)
        assertEquals("The cat batted at the toy.", result[0].example)
    }

    @Test
    fun `example is null when not present`() {
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "noun",
                    "definitions": [
                        {"definition": "A common round fruit produced by the tree Malus domestica."}
                    ]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(1, result.size)
        assertNull(result[0].example)
    }

    // -------------------------------------------------------
    // 5. Caps at 3 parts of speech
    // -------------------------------------------------------
    @Test
    fun `returns at most 3 parts of speech`() {
        val json = """[{
            "meanings": [
                {
                    "partOfSpeech": "noun",
                    "definitions": [{"definition": "A thing used for sitting on."}]
                },
                {
                    "partOfSpeech": "verb",
                    "definitions": [{"definition": "To cause someone to sit down."}]
                },
                {
                    "partOfSpeech": "adjective",
                    "definitions": [{"definition": "Having been arranged beforehand."}]
                },
                {
                    "partOfSpeech": "adverb",
                    "definitions": [{"definition": "In a fixed position or manner."}]
                }
            ]
        }]"""

        val result = DictionaryApiClient.parseDefinitions(json)

        assertEquals(3, result.size)
    }

    // -------------------------------------------------------
    // 6. Handles malformed JSON and empty input
    // -------------------------------------------------------
    @Test
    fun `returns empty list for malformed JSON`() {
        val result = DictionaryApiClient.parseDefinitions("not json at all")
        assertEquals(0, result.size)
    }

    @Test
    fun `returns empty list for empty array`() {
        val result = DictionaryApiClient.parseDefinitions("[]")
        assertEquals(0, result.size)
    }

    @Test
    fun `returns empty list for missing meanings`() {
        val json = """[{"word": "test"}]"""
        val result = DictionaryApiClient.parseDefinitions(json)
        assertEquals(0, result.size)
    }
}
