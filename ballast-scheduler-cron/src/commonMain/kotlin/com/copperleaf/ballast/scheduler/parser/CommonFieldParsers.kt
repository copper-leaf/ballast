package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.utils.number
import com.copperleaf.kudzu.KudzuPlatform
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.chars.DigitParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.AtLeastParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.maybe.MaybeParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser
import com.copperleaf.kudzu.parser.text.BaseTextParser
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.number

@Suppress("UNCHECKED_CAST")
internal object CommonFieldParsers {

    val numberParser: Parser<ValueNode<Int>> = MappedParser(
        parser = AtLeastParser(
            DigitParser(),
            minSize = 1
        ),
        mapperFunction = { digitsNode ->
            digitsNode.text.toInt()
        }
    )
    val stepValueParser: Parser<ValueNode<Int>> = MappedParser(
        parser = SequenceParser(
            CharInParser('/'),
            numberParser
        ),
        mapperFunction = { (_, _, number) ->
            number.value
        }
    )
    val maybeStepValueParser: Parser<ValueNode<Int>> = MappedParser(
        parser = MaybeParser(
            stepValueParser
        ),
        mapperFunction = { maybeNode ->
            maybeNode.node?.value ?: 1
        }
    )

    val monthNameParser: Parser<ValueNode<Int>> = MappedParser(
        parser = ExactChoiceParser(
            MappedParser(EnumValueParser("JAN")) { Month.JANUARY },
            MappedParser(EnumValueParser("FEB")) { Month.FEBRUARY },
            MappedParser(EnumValueParser("MAR")) { Month.MARCH },
            MappedParser(EnumValueParser("APR")) { Month.APRIL },
            MappedParser(EnumValueParser("MAY")) { Month.MAY },
            MappedParser(EnumValueParser("JUN")) { Month.JUNE },
            MappedParser(EnumValueParser("JUL")) { Month.JULY },
            MappedParser(EnumValueParser("AUG")) { Month.AUGUST },
            MappedParser(EnumValueParser("SEP")) { Month.SEPTEMBER },
            MappedParser(EnumValueParser("OCT")) { Month.OCTOBER },
            MappedParser(EnumValueParser("NOV")) { Month.NOVEMBER },
            MappedParser(EnumValueParser("DEC")) { Month.DECEMBER },
        ),
        mapperFunction = { choiceNode ->
            (choiceNode.node as ValueNode<Month>).value.number
        }
    )

    val dayOfWeekNameParser: Parser<ValueNode<Int>> = MappedParser(
        parser = ExactChoiceParser(
            MappedParser(EnumValueParser("SUN")) { DayOfWeek.SUNDAY },
            MappedParser(EnumValueParser("MON")) { DayOfWeek.MONDAY },
            MappedParser(EnumValueParser("TUE")) { DayOfWeek.TUESDAY },
            MappedParser(EnumValueParser("WED")) { DayOfWeek.WEDNESDAY },
            MappedParser(EnumValueParser("THU")) { DayOfWeek.THURSDAY },
            MappedParser(EnumValueParser("FRI")) { DayOfWeek.FRIDAY },
            MappedParser(EnumValueParser("SAT")) { DayOfWeek.SATURDAY },
        ),
        mapperFunction = { choiceNode ->
            (choiceNode.node as ValueNode<DayOfWeek>).value.number
        }
    )

    private class EnumValueParser(
        val enumValue: String
    ) : BaseTextParser(
        isValidChar = { _, char -> KudzuPlatform.isLetter(char) },
        isValidText = { it.equals(enumValue, ignoreCase = true) },
        allowEmptyInput = false,
        invalidTextErrorMessage = { "Expected '$enumValue' token, got '$it'" },
    )
}
