package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.DayOfMonthField
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DayOfMonthFieldParserTest {

    @Test
    fun exactValueTest() {
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.exactValue, "1", listOf(1), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.exactValue, "2", listOf(2), false)
    }

    @Test
    fun rangeValueTest() {
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.rangeValue, "2-4", listOf(2, 3, 4), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.rangeValue, "2-10/2", listOf(2, 4, 6, 8, 10), false)
    }

    @Test
    fun wildcardValueTest() {
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.wildcardValue, "*", listOf(
            1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23, 24, 25,
            26, 27, 28, 29, 30, 31
        ), true)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.wildcardValue, "*/4", listOf(1, 5, 9, 13, 17, 21, 25, 29), true)
    }

    @Test
    fun singleDayOfMonthFieldParserTest() {
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.singleDayOfMonthFieldParser, "1", listOf(1), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.singleDayOfMonthFieldParser, "2", listOf(2), false)

        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.singleDayOfMonthFieldParser, "2-4", listOf(2, 3, 4), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.singleDayOfMonthFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.singleDayOfMonthFieldParser, "*", listOf(
            1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23, 24, 25,
            26, 27, 28, 29, 30, 31
        ), true)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.singleDayOfMonthFieldParser, "*/4", listOf(1, 5, 9, 13, 17, 21, 25, 29), true)
    }

    @Test
    fun listOfDayOfMonthFieldParserTest() {
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "1", listOf(1), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "2", listOf(2), false)

        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "2-4", listOf(2, 3, 4), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "*", listOf(
            1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23, 24, 25,
            26, 27, 28, 29, 30, 31
        ), true)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "*/4", listOf(1, 5, 9, 13, 17, 21, 25, 29), true)

        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "1,5,8", listOf(1, 5, 8), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "1,3-5,8-12/2", listOf(1, 3, 4, 5, 8, 10, 12), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "4,3,1,2", listOf(1, 2, 3, 4), false)
        assertDayOfMonthFieldParserSuccess(DayOfMonthFieldParser.listOfDayOfMonthFieldParser, "4,*/4,*/6", listOf(1, 4, 5, 7, 9, 13, 17, 19, 21, 25, 29, 31), false)
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

    private fun assertDayOfMonthFieldParserSuccess(
        parser: Parser<ValueNode<DayOfMonthField>>,
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
