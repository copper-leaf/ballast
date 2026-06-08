package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.MinuteField
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinuteFieldParserTest {

    @Test
    fun exactValueTest() {
        assertMinuteFieldParserSuccess(MinuteFieldParser.exactValue, "1", listOf(1), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.exactValue, "2", listOf(2), false)
    }

    @Test
    fun rangeValueTest() {
        assertMinuteFieldParserSuccess(MinuteFieldParser.rangeValue, "2-4", listOf(2, 3, 4), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.rangeValue, "2-10/2", listOf(2, 4, 6, 8, 10), false)
    }

    @Test
    fun wildcardValueTest() {
        assertMinuteFieldParserSuccess(MinuteFieldParser.wildcardValue, "*", listOf(
            0, 1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23, 24, 25,
            26, 27, 28, 29, 30,
            31, 32, 33, 34, 35,
            36, 37, 38, 39, 40,
            41, 42, 43, 44, 45,
            46, 47, 48, 49, 50,
            51, 52, 53, 54, 55,
            56, 57, 58, 59
        ), true)
        assertMinuteFieldParserSuccess(MinuteFieldParser.wildcardValue, "*/4", listOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56), true)
    }

    @Test
    fun singleMinuteFieldParserTest() {
        assertMinuteFieldParserSuccess(MinuteFieldParser.singleMinuteFieldParser, "1", listOf(1), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.singleMinuteFieldParser, "2", listOf(2), false)

        assertMinuteFieldParserSuccess(MinuteFieldParser.singleMinuteFieldParser, "2-4", listOf(2, 3, 4), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.singleMinuteFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertMinuteFieldParserSuccess(MinuteFieldParser.singleMinuteFieldParser, "*", listOf(
            0, 1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23, 24, 25,
            26, 27, 28, 29, 30,
            31, 32, 33, 34, 35,
            36, 37, 38, 39, 40,
            41, 42, 43, 44, 45,
            46, 47, 48, 49, 50,
            51, 52, 53, 54, 55,
            56, 57, 58, 59
        ), true)
        assertMinuteFieldParserSuccess(MinuteFieldParser.singleMinuteFieldParser, "*/4", listOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56), true)
    }

    @Test
    fun listOfMinuteFieldParserTest() {
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "1", listOf(1), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "2", listOf(2), false)

        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "2-4", listOf(2, 3, 4), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "*", listOf(
            0, 1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23, 24, 25,
            26, 27, 28, 29, 30,
            31, 32, 33, 34, 35,
            36, 37, 38, 39, 40,
            41, 42, 43, 44, 45,
            46, 47, 48, 49, 50,
            51, 52, 53, 54, 55,
            56, 57, 58, 59
        ), true)
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "*/4", listOf(0, 4, 8, 12, 16, 20, 24, 28, 32, 36, 40, 44, 48, 52, 56), true)

        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "1,5,8", listOf(1, 5, 8), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "1,3-5,8-12/2", listOf(1, 3, 4, 5, 8, 10, 12), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "4,3,1,2", listOf(1, 2, 3, 4), false)
        assertMinuteFieldParserSuccess(MinuteFieldParser.listOfMinuteFieldParser, "4,*/4,*/6", listOf(0, 4, 6, 8, 12, 16, 18, 20, 24, 28, 30, 32, 36, 40, 42, 44, 48, 52, 54, 56), false)
    }

// utils
// ---------------------------------------------------------------------------------------------------------------------

    private fun <T> assertParseSuccess(
        parser: Parser<ValueNode<T>>,
        input: String,
        expectedValues: T,
    ) {
        val (node, remainingText) = parser.parse(ParserContext.fromString(input))
        assertTrue { remainingText.isEmpty() }
        assertEquals(
            actual = node.value,
            expected = expectedValues,
        )
    }

    private fun <T> assertParseThrows(
        parser: Parser<ValueNode<T>>,
        input: String,
    ) {
        assertFails { parser.parse(ParserContext.fromString(input)) }
    }

    private fun <T> assertParseIncomplete(
        parser: Parser<ValueNode<T>>,
        input: String,
    ) {
        val (_, remainingText) = parser.parse(ParserContext.fromString(input))
        assertFalse { remainingText.isEmpty() }
    }

    private fun assertMinuteFieldParserSuccess(
        parser: Parser<ValueNode<MinuteField>>,
        input: String,
        expectedValues: List<Int>,
        expectedWildcard: Boolean = false,
    ) {
        val (node, remainingText) = parser.parse(ParserContext.fromString(input))
        assertTrue { remainingText.isEmpty() }
        assertEquals(
            actual = node.value.values,
            expected = expectedValues,
        )
        assertEquals(
            actual = node.value.wildcard,
            expected = expectedWildcard,
        )
    }
}
