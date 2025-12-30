package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonFieldParserTest {

    @Test
    fun numberParserTest() {
        assertParseSuccess(CommonFieldParsers.numberParser, "4", 4)
        assertParseSuccess(CommonFieldParsers.numberParser, "8", 8)
        assertParseSuccess(CommonFieldParsers.numberParser, "15", 15)
        assertParseSuccess(CommonFieldParsers.numberParser, "16", 16)
        assertParseSuccess(CommonFieldParsers.numberParser, "23", 23)
        assertParseSuccess(CommonFieldParsers.numberParser, "42", 42)
        assertParseSuccess(CommonFieldParsers.numberParser, "0", 0)

        assertParseThrows(CommonFieldParsers.numberParser, "-1")
        assertParseThrows(CommonFieldParsers.numberParser, "")
    }

    @Test
    fun stepValueParserTest() {
        assertParseSuccess(CommonFieldParsers.stepValueParser, "/4", 4)
        assertParseSuccess(CommonFieldParsers.stepValueParser, "/8", 8)
        assertParseSuccess(CommonFieldParsers.stepValueParser, "/15", 15)
        assertParseSuccess(CommonFieldParsers.stepValueParser, "/16", 16)
        assertParseSuccess(CommonFieldParsers.stepValueParser, "/23", 23)
        assertParseSuccess(CommonFieldParsers.stepValueParser, "/42", 42)

        assertParseThrows(CommonFieldParsers.stepValueParser, "/-1")
        assertParseThrows(CommonFieldParsers.stepValueParser, "/")
        assertParseThrows(CommonFieldParsers.stepValueParser, "1")
    }

    @Test
    fun maybeStepValueParserTest() {
        assertParseSuccess(CommonFieldParsers.maybeStepValueParser, "/4", 4)
        assertParseSuccess(CommonFieldParsers.maybeStepValueParser, "/8", 8)
        assertParseSuccess(CommonFieldParsers.maybeStepValueParser, "/15", 15)
        assertParseSuccess(CommonFieldParsers.maybeStepValueParser, "/16", 16)
        assertParseSuccess(CommonFieldParsers.maybeStepValueParser, "/23", 23)
        assertParseSuccess(CommonFieldParsers.maybeStepValueParser, "/42", 42)

        assertParseThrows(CommonFieldParsers.maybeStepValueParser, "/-1")
        assertParseThrows(CommonFieldParsers.maybeStepValueParser, "/")
        assertParseIncomplete(CommonFieldParsers.maybeStepValueParser, "1")
        assertParseIncomplete(CommonFieldParsers.maybeStepValueParser, "2")
    }

    @Test
    fun monthNameParserTest() {
        assertParseSuccess(CommonFieldParsers.monthNameParser, "jan", 1)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "feb", 2)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "mar", 3)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "apr", 4)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "may", 5)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "jun", 6)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "jul", 7)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "aug", 8)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "sep", 9)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "oct", 10)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "nov", 11)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "dec", 12)

        assertParseSuccess(CommonFieldParsers.monthNameParser, "JAN", 1)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "FEB", 2)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "MAR", 3)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "APR", 4)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "MAY", 5)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "JUN", 6)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "JUL", 7)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "AUG", 8)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "SEP", 9)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "OCT", 10)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "NOV", 11)
        assertParseSuccess(CommonFieldParsers.monthNameParser, "DEC", 12)
    }

    @Test
    fun dayOfWeekNameParser() {
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "sun", 0)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "mon", 1)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "tue", 2)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "wed", 3)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "thu", 4)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "fri", 5)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "sat", 6)

        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "SUN", 0)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "MON", 1)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "TUE", 2)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "WED", 3)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "THU", 4)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "FRI", 5)
        assertParseSuccess(CommonFieldParsers.dayOfWeekNameParser, "SAT", 6)
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
}
