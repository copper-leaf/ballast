package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.DayOfWeekField
import com.copperleaf.kudzu.node.choice.Choice2Node
import com.copperleaf.kudzu.node.choice.Choice3Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

internal object DayOfWeekFieldParser {

    internal val dayOfWeekNameOrValueParser: Parser<ValueNode<Int>> = MappedParser(
        parser = ExactChoiceParser(
            CommonFieldParsers.dayOfWeekNameParser,
            CommonFieldParsers.numberParser,
        ),
        mapperFunction = { choiceNode ->
            when (choiceNode) {
                is Choice2Node.Option1 -> choiceNode.node.value
                is Choice2Node.Option2 -> choiceNode.node.value
            }
        }
    )

    internal val exactValue: Parser<ValueNode<DayOfWeekField>> = MappedParser(
        dayOfWeekNameOrValueParser
    ) { node ->
        DayOfWeekField.exactValue(node.value)
    }

    internal val rangeValue: Parser<ValueNode<DayOfWeekField>> = MappedParser(
        SequenceParser(
            dayOfWeekNameOrValueParser,
            CharInParser('-'),
            dayOfWeekNameOrValueParser,
            CommonFieldParsers.maybeStepValueParser
        ),
    ) { (_, startValue, _, endValue, stepValue) ->
        DayOfWeekField.range(min = startValue.value, max = endValue.value, step = stepValue.value)
    }

    internal val wildcardValue: Parser<ValueNode<DayOfWeekField>> = MappedParser(
        SequenceParser(
            CharInParser('*'),
            CommonFieldParsers.maybeStepValueParser,
        ),
    ) { (_, _, stepValue) ->
        DayOfWeekField.anyValue(step = stepValue.value)
    }

    internal val singleDayOfWeekFieldParser: Parser<ValueNode<DayOfWeekField>> = MappedParser(
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

    internal val listOfDayOfWeekFieldParser: Parser<ValueNode<DayOfWeekField>> = MappedParser(
        parser = SeparatedByParser(
            term = singleDayOfWeekFieldParser,
            separator = CharInParser(','),
        ),
        mapperFunction = { manyNode ->
            val dayOfWeekFieldNodes = manyNode.nodeList.map { it.value }
            DayOfWeekField.series(dayOfWeekFieldNodes)
        }
    )
}
