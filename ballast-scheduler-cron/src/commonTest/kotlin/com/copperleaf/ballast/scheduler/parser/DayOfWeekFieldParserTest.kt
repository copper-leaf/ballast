package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.DayOfWeekField
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DayOfWeekFieldParserTest {

    @Test
    fun dayOfWeekNameOrValueParserTest() {
        assertParseSuccess(DayOfWeekFieldParser.dayOfWeekNameOrValueParser, "0", 0)
        assertParseSuccess(DayOfWeekFieldParser.dayOfWeekNameOrValueParser, "sun", 0)
        assertParseSuccess(DayOfWeekFieldParser.dayOfWeekNameOrValueParser, "SUN", 0)
        assertParseSuccess(DayOfWeekFieldParser.dayOfWeekNameOrValueParser, "2", 2)
        assertParseSuccess(DayOfWeekFieldParser.dayOfWeekNameOrValueParser, "tue", 2)
        assertParseSuccess(DayOfWeekFieldParser.dayOfWeekNameOrValueParser, "TUE", 2)
    }

    @Test
    fun exactValueTest() {
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.exactValue, "0", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.exactValue, "sun", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.exactValue, "SUN", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.exactValue, "2", listOf(2), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.exactValue, "tue", listOf(2), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.exactValue, "TUE", listOf(2), false)
    }

    @Test
    fun rangeValueTest() {
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.rangeValue, "2-4", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.rangeValue, "tue-thu", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.rangeValue, "TUE-THU", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.rangeValue, "2-THU", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.rangeValue, "tue-4", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.rangeValue, "2-6/2", listOf(2, 4, 6), false)
    }

    @Test
    fun wildcardValueTest() {
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.wildcardValue, "*", listOf(0, 1, 2, 3, 4, 5, 6), true)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.wildcardValue, "*/4", listOf(0, 4), true)
    }

    @Test
    fun singleDayOfWeekFieldParserTest() {
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "0", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "sun", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "SUN", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "2", listOf(2), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "tue", listOf(2), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "TUE", listOf(2), false)

        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "2-4", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "tue-thu", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "TUE-THU", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "2-THU", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "TUE-4", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "2-6/2", listOf(2, 4, 6), false)

        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "*", listOf(0, 1, 2, 3, 4, 5, 6), true)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.singleDayOfWeekFieldParser, "*/4", listOf(0, 4), true)
    }

    @Test
    fun listOfDayOfWeekFieldParserTest() {
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "0", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "sun", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "SUN", listOf(0), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "2", listOf(2), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "tue", listOf(2), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "TUE", listOf(2), false)

        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "2-4", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "tue-thu", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "TUE-THU", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "2-THU", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "TUE-4", listOf(2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "2-6/2", listOf(2, 4, 6), false)

        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "*", listOf(0, 1, 2, 3, 4, 5, 6), true)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "*/4", listOf(0, 4), true)

        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "1,5,6", listOf(1, 5, 6), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "1,3-5,2-6/2", listOf(1, 2, 3, 4, 5, 6), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "4,3,1,2", listOf(1, 2, 3, 4), false)
        assertDayOfWeekFieldParserSuccess(DayOfWeekFieldParser.listOfDayOfWeekFieldParser, "4,*/4,*/6", listOf(0, 4, 6), false)
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

    private fun assertDayOfWeekFieldParserSuccess(
        parser: Parser<ValueNode<DayOfWeekField>>,
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
