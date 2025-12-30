package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.HourField
import com.copperleaf.kudzu.node.choice.Choice3Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

internal object HourFieldParser {

    internal val hourValueParser: Parser<ValueNode<Int>> = CommonFieldParsers.numberParser

    internal val exactValue: Parser<ValueNode<HourField>> = MappedParser(
        hourValueParser
    ) { node ->
        HourField.exactValue(node.value)
    }

    internal val rangeValue: Parser<ValueNode<HourField>> = MappedParser(
        SequenceParser(
            hourValueParser,
            CharInParser('-'),
            hourValueParser,
            CommonFieldParsers.maybeStepValueParser
        ),
    ) { (_, startValue, _, endValue, stepValue) ->
        HourField.range(min = startValue.value, max = endValue.value, step = stepValue.value)
    }

    internal val wildcardValue: Parser<ValueNode<HourField>> = MappedParser(
        SequenceParser(
            CharInParser('*'),
            CommonFieldParsers.maybeStepValueParser,
        ),
    ) { (_, _, stepValue) ->
        HourField.anyValue(step = stepValue.value)
    }

    internal val singleHourFieldParser: Parser<ValueNode<HourField>> = MappedParser(
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

    internal val listOfHourFieldParser: Parser<ValueNode<HourField>> = MappedParser(
        parser = SeparatedByParser(
            term = singleHourFieldParser,
            separator = CharInParser(','),
        ),
        mapperFunction = { manyNode ->
            val hourFieldNodes = manyNode.nodeList.map { it.value }
            HourField.series(hourFieldNodes)
        }
    )
}
