package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.CronExpression
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser
import com.copperleaf.kudzu.parser.text.RequiredWhitespaceParser
import kotlinx.datetime.TimeZone

internal object CronExpressionParser {

    internal val cronExpressionParser = MappedParser(
        parser = SequenceParser(
            MinuteFieldParser.listOfMinuteFieldParser,
            RequiredWhitespaceParser(),
            HourFieldParser.listOfHourFieldParser,
            RequiredWhitespaceParser(),
            DayOfMonthFieldParser.listOfDayOfMonthFieldParser,
            RequiredWhitespaceParser(),
            MonthFieldParser.listOfMonthFieldParser,
            RequiredWhitespaceParser(),
            DayOfWeekFieldParser.listOfDayOfWeekFieldParser,
        ),
        mapperFunction = { (_, minute, _, hour, _, dayOfMonth, _, month, _, dayOfWeek) ->
            CronExpression(
                minute = minute.value,
                hour = hour.value,
                dayOfMonth = dayOfMonth.value,
                month = month.value,
                dayOfWeek = dayOfWeek.value,
            )
        }
    )

    internal fun parse(expression: String, timeZone: TimeZone): CronExpression {
        val (node, remainingText) = cronExpressionParser.parse(ParserContext.fromString(expression))
        check(remainingText.isEmpty()) {
            "Unexpected trailing text after cron expression: '$remainingText'"
        }
        return node.value.copy(timeZone = timeZone)
    }
}
