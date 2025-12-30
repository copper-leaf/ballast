package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.DayOfMonthField
import com.copperleaf.kudzu.node.choice.Choice3Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

internal object DayOfMonthFieldParser {

    internal val dayOfMonthValueParser: Parser<ValueNode<Int>> = CommonFieldParsers.numberParser

    internal val exactValue: Parser<ValueNode<DayOfMonthField>> = MappedParser(
        dayOfMonthValueParser
    ) { node ->
        DayOfMonthField.exactValue(node.value)
    }

    internal val rangeValue: Parser<ValueNode<DayOfMonthField>> = MappedParser(
        SequenceParser(
            dayOfMonthValueParser,
            CharInParser('-'),
            dayOfMonthValueParser,
            CommonFieldParsers.maybeStepValueParser
        ),
    ) { (_, startValue, _, endValue, stepValue) ->
        DayOfMonthField.range(min = startValue.value, max = endValue.value, step = stepValue.value)
    }

    internal val wildcardValue: Parser<ValueNode<DayOfMonthField>> = MappedParser(
        SequenceParser(
            CharInParser('*'),
            CommonFieldParsers.maybeStepValueParser,
        ),
    ) { (_, _, stepValue) ->
        DayOfMonthField.anyValue(step = stepValue.value)
    }

    internal val singleDayOfMonthFieldParser: Parser<ValueNode<DayOfMonthField>> = MappedParser(
        parser = ExactChoiceParser(
            wildcardValue,
            rangeValue,
            exactValue,
        ),
        mapperFunction = { choiceNode ->
            when (choiceNode) {
                is Choice3Node.Option1 -> choiceNode.node.value
                is Choice3Node.Option2 -> choiceNode.node.value
                is Choice3Node.Option3 -> choiceNode.node.value
            }
        }
    )

    internal val listOfDayOfMonthFieldParser: Parser<ValueNode<DayOfMonthField>> = MappedParser(
        parser = SeparatedByParser(
            term = singleDayOfMonthFieldParser,
            separator = CharInParser(','),
        ),
        mapperFunction = { manyNode ->
            val dayOfMonthFieldNodes = manyNode.nodeList.map { it.value }
            DayOfMonthField.series(dayOfMonthFieldNodes)
        }
    )
}
