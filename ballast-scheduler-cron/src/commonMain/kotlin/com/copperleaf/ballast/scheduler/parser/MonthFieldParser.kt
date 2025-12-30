package com.copperleaf.ballast.scheduler.parser

import com.copperleaf.ballast.scheduler.schedule.MonthField
import com.copperleaf.kudzu.node.choice.Choice2Node
import com.copperleaf.kudzu.node.choice.Choice3Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser

internal object MonthFieldParser {

    internal val monthNameOrValueParser: Parser<ValueNode<Int>> = MappedParser(
        parser = ExactChoiceParser(
            CommonFieldParsers.monthNameParser,
            CommonFieldParsers.numberParser,
        ),
        mapperFunction = { choiceNode ->
            when (choiceNode) {
                is Choice2Node.Option1 -> choiceNode.node.value
                is Choice2Node.Option2 -> choiceNode.node.value
            }
        }
    )

    internal val exactValue: Parser<ValueNode<MonthField>> = MappedParser(
        monthNameOrValueParser
    ) { node ->
        MonthField.exactValue(node.value)
    }

    internal val rangeValue: Parser<ValueNode<MonthField>> = MappedParser(
        SequenceParser(
            monthNameOrValueParser,
            CharInParser('-'),
            monthNameOrValueParser,
            CommonFieldParsers.maybeStepValueParser
        ),
    ) { (_, startValue, _, endValue, stepValue) ->
        MonthField.range(min = startValue.value, max = endValue.value, step = stepValue.value)
    }

    internal val wildcardValue: Parser<ValueNode<MonthField>> = MappedParser(
        SequenceParser(
            CharInParser('*'),
            CommonFieldParsers.maybeStepValueParser,
        ),
    ) { (_, _, stepValue) ->
        MonthField.anyValue(step = stepValue.value)
    }

    internal val singleMonthFieldParser: Parser<ValueNode<MonthField>> = MappedParser(
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

    internal val listOfMonthFieldParser: Parser<ValueNode<MonthField>> = MappedParser(
        parser = SeparatedByParser(
            term = singleMonthFieldParser,
            separator = CharInParser(','),
        ),
        mapperFunction = { manyNode ->
            val monthFieldNodes = manyNode.nodeList.map { it.value }
            MonthField.series(monthFieldNodes)
        }
    )
}
