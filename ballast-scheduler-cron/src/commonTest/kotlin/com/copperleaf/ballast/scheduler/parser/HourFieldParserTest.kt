package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.HourField
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HourFieldParserTest {

    @Test
    fun exactValueTest() {
        assertHourFieldParserSuccess(HourFieldParser.exactValue, "1", listOf(1), false)
        assertHourFieldParserSuccess(HourFieldParser.exactValue, "2", listOf(2), false)
    }

    @Test
    fun rangeValueTest() {
        assertHourFieldParserSuccess(HourFieldParser.rangeValue, "2-4", listOf(2, 3, 4), false)
        assertHourFieldParserSuccess(HourFieldParser.rangeValue, "2-10/2", listOf(2, 4, 6, 8, 10), false)
    }

    @Test
    fun wildcardValueTest() {
        assertHourFieldParserSuccess(HourFieldParser.wildcardValue, "*", listOf(
            0, 1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23,
        ), true)
        assertHourFieldParserSuccess(HourFieldParser.wildcardValue, "*/4", listOf(0, 4, 8, 12, 16, 20), true)
    }

    @Test
    fun singleHourFieldParserTest() {
        assertHourFieldParserSuccess(HourFieldParser.singleHourFieldParser, "1", listOf(1), false)
        assertHourFieldParserSuccess(HourFieldParser.singleHourFieldParser, "2", listOf(2), false)

        assertHourFieldParserSuccess(HourFieldParser.singleHourFieldParser, "2-4", listOf(2, 3, 4), false)
        assertHourFieldParserSuccess(HourFieldParser.singleHourFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertHourFieldParserSuccess(HourFieldParser.singleHourFieldParser, "*", listOf(
            0, 1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23,
        ), true)
        assertHourFieldParserSuccess(HourFieldParser.singleHourFieldParser, "*/4", listOf(0, 4, 8, 12, 16, 20), true)
    }

    @Test
    fun listOfHourFieldParserTest() {
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "1", listOf(1), false)
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "2", listOf(2), false)

        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "2-4", listOf(2, 3, 4), false)
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "*", listOf(
            0, 1, 2, 3, 4, 5,
            6, 7, 8, 9, 10,
            11, 12, 13, 14, 15,
            16, 17, 18, 19, 20,
            21, 22, 23,
        ), true)
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "*/4", listOf(0, 4, 8, 12, 16, 20), true)

        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "1,5,8", listOf(1, 5, 8), false)
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "1,3-5,8-12/2", listOf(1, 3, 4, 5, 8, 10, 12), false)
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "4,3,1,2", listOf(1, 2, 3, 4), false)
        assertHourFieldParserSuccess(HourFieldParser.listOfHourFieldParser, "4,*/4,*/6", listOf(0, 4, 6, 8, 12, 16, 18, 20), false)
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

    private fun assertHourFieldParserSuccess(
        parser: Parser<ValueNode<HourField>>,
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
