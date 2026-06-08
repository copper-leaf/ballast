package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.MonthField
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MonthFieldParserTest {

    @Test
    fun monthNameOrValueParserTest() {
        assertParseSuccess(MonthFieldParser.monthNameOrValueParser, "1", 1)
        assertParseSuccess(MonthFieldParser.monthNameOrValueParser, "jan", 1)
        assertParseSuccess(MonthFieldParser.monthNameOrValueParser, "JAN", 1)
        assertParseSuccess(MonthFieldParser.monthNameOrValueParser, "2", 2)
        assertParseSuccess(MonthFieldParser.monthNameOrValueParser, "feb", 2)
        assertParseSuccess(MonthFieldParser.monthNameOrValueParser, "FEB", 2)
    }

    @Test
    fun exactValueTest() {
        assertMonthFieldParserSuccess(MonthFieldParser.exactValue, "1", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.exactValue, "jan", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.exactValue, "JAN", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.exactValue, "2", listOf(2), false)
        assertMonthFieldParserSuccess(MonthFieldParser.exactValue, "feb", listOf(2), false)
        assertMonthFieldParserSuccess(MonthFieldParser.exactValue, "FEB", listOf(2), false)
    }

    @Test
    fun rangeValueTest() {
        assertMonthFieldParserSuccess(MonthFieldParser.rangeValue, "2-4", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.rangeValue, "feb-apr", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.rangeValue, "FEB-APR", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.rangeValue, "2-APR", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.rangeValue, "FEB-4", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.rangeValue, "2-10/2", listOf(2, 4, 6, 8, 10), false)
    }

    @Test
    fun wildcardValueTest() {
        assertMonthFieldParserSuccess(MonthFieldParser.wildcardValue, "*", listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), true)
        assertMonthFieldParserSuccess(MonthFieldParser.wildcardValue, "*/4", listOf(1, 5, 9), true)
    }

    @Test
    fun singleMonthFieldParserTest() {
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "1", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "jan", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "JAN", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "2", listOf(2), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "feb", listOf(2), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "FEB", listOf(2), false)

        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "2-4", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "feb-apr", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "FEB-APR", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "2-APR", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "FEB-4", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "*", listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), true)
        assertMonthFieldParserSuccess(MonthFieldParser.singleMonthFieldParser, "*/4", listOf(1, 5, 9), true)
    }

    @Test
    fun listOfMonthFieldParserTest() {
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "1", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "jan", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "JAN", listOf(1), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "2", listOf(2), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "feb", listOf(2), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "FEB", listOf(2), false)

        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "2-4", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "feb-apr", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "FEB-APR", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "2-APR", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "FEB-4", listOf(2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "2-10/2", listOf(2, 4, 6, 8, 10), false)

        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "*", listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), true)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "*/4", listOf(1, 5, 9), true)

        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "1,5,8", listOf(1, 5, 8), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "1,3-5,8-12/2", listOf(1, 3, 4, 5, 8, 10, 12), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "4,3,1,2", listOf(1, 2, 3, 4), false)
        assertMonthFieldParserSuccess(MonthFieldParser.listOfMonthFieldParser, "4,*/4,*/6", listOf(1, 4, 5, 7, 9), false)
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

    private fun assertMonthFieldParserSuccess(
        parser: Parser<ValueNode<MonthField>>,
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
