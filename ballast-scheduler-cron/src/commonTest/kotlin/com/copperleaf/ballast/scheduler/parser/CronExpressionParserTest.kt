package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.CronExpression
import com.copperleaf.ballast.scheduler.schedule.DayOfMonthField
import com.copperleaf.ballast.scheduler.schedule.DayOfWeekField
import com.copperleaf.ballast.scheduler.schedule.HourField
import com.copperleaf.ballast.scheduler.schedule.MinuteField
import com.copperleaf.ballast.scheduler.schedule.MonthField
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CronExpressionParserTest {

    @Test
    fun cronExpressionParserTest() {
        assertParseSuccess("* * * * *") {
            CronExpression()
        }
        assertParseSuccess("* */4 * * *") {
            CronExpression(hour = HourField.anyValue(step = 4))
        }
        assertParseSuccess("* 6-12/2 * * *") {
            CronExpression(hour = HourField.range(6, 12, 2))
        }
        assertParseSuccess("*/15 6-12/2 15 JAN,JUN-SEP/2,DEC SUN,TUE-THU/2,SAT") {
            CronExpression(
                minute = MinuteField.anyValue(step = 15),
                hour = HourField.range(6, 12, 2),
                dayOfMonth = DayOfMonthField.exactValue(15),
                month = MonthField(1, 6, 8, 12, wildcard = false),
                dayOfWeek = DayOfWeekField(0, 2, 4, 6, wildcard = false),
            )
        }
    }

// utils
// ---------------------------------------------------------------------------------------------------------------------

    private fun assertParseSuccess(
        input: String,
        expected: () -> CronExpression,
    ) {
        val (node, remainingText) = CronExpressionParser.cronExpressionParser.parse(ParserContext.fromString(input))
        assertTrue { remainingText.isEmpty() }
        assertEquals(
            actual = node.value,
            expected = expected(),
        )

        assertEquals(
            actual = CronExpression.parse(input),
            expected = expected(),
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
