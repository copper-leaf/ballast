package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.MinuteField
import com.copperleaf.kudzu.node.choice.Choice3Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

internal object MinuteFieldParser {

    internal val hourValueParser: Parser<ValueNode<Int>> = CommonFieldParsers.numberParser

    internal val exactValue: Parser<ValueNode<MinuteField>> = MappedParser(
        hourValueParser
    ) { node ->
        MinuteField.exactValue(node.value)
    }

    internal val rangeValue: Parser<ValueNode<MinuteField>> = MappedParser(
        SequenceParser(
            hourValueParser,
            CharInParser('-'),
            hourValueParser,
            CommonFieldParsers.maybeStepValueParser
        ),
    ) { (_, startValue, _, endValue, stepValue) ->
        MinuteField.range(min = startValue.value, max = endValue.value, step = stepValue.value)
    }

    internal val wildcardValue: Parser<ValueNode<MinuteField>> = MappedParser(
        SequenceParser(
            CharInParser('*'),
            CommonFieldParsers.maybeStepValueParser,
        ),
    ) { (_, _, stepValue) ->
        MinuteField.anyValue(step = stepValue.value)
    }

    internal val singleMinuteFieldParser: Parser<ValueNode<MinuteField>> = MappedParser(
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

    internal val listOfMinuteFieldParser: Parser<ValueNode<MinuteField>> = MappedParser(
        parser = SeparatedByParser(
            term = singleMinuteFieldParser,
            separator = CharInParser(','),
        ),
        mapperFunction = { manyNode ->
            val minuteFieldNodes = manyNode.nodeList.map { it.value }
            MinuteField.series(minuteFieldNodes)
        }
    )
}
