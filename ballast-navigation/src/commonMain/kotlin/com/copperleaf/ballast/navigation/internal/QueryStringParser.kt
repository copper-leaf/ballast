package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.QueryParameter
import com.copperleaf.kudzu.node.choice.Choice2Node
import com.copperleaf.kudzu.node.choice.Choice3Node
import com.copperleaf.kudzu.node.choice.Choice4Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.choice.PredictiveChoiceParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser
import com.copperleaf.kudzu.parser.text.IdentifierTokenParser
import com.copperleaf.kudzu.parser.text.LiteralTokenParser

internal object QueryStringParser {

// Query Parameters
// ---------------------------------------------------------------------------------------------------------------------

    private val staticQueryParser: Parser<ValueNode<QueryParameter.Static>> = MappedParser(
        SequenceParser(
            IdentifierTokenParser(),
            CharInParser('='),
            PredictiveChoiceParser(
                IdentifierTokenParser(),
                SequenceParser(
                    CharInParser('['),
                    SeparatedByParser(
                        term = IdentifierTokenParser(),
                        separator = CharInParser(',')
                    ),
                    CharInParser(']'),
                )
            ),
        )
    ) { (_, key, _, value) ->
        QueryParameter.Static(
            name = key.text,
            values = when (value) {
                is Choice2Node.Option1 -> listOf(value.text)
                is Choice2Node.Option2 -> value.node.node2.nodeList.map { it.text }.sorted()
            },
        )
    }

    private val parameterQueryParser: Parser<ValueNode<QueryParameter.Parameter>> = MappedParser(
        SequenceParser(
            IdentifierTokenParser(),
            CharInParser('='),
            CharInParser('{'),
            ExactChoiceParser(
                LiteralTokenParser("[!]"), // 1+ values
                LiteralTokenParser("[?]"), // 0+ values
                LiteralTokenParser("!"), // 1 value
                LiteralTokenParser("?"), // 0 or 1 value
            ),
            CharInParser('}'),
        )
    ) { (_, key, _, _, value, _) ->
        QueryParameter.Parameter(
            name = key.text,
            optional = when (value) {
                is Choice4Node.Option1 -> false
                is Choice4Node.Option2 -> true
                is Choice4Node.Option3 -> false
                is Choice4Node.Option4 -> true
            },
            allowMultiple = when (value) {
                is Choice4Node.Option1 -> true
                is Choice4Node.Option2 -> true
                is Choice4Node.Option3 -> false
                is Choice4Node.Option4 -> false
            },
        )
    }

    private val remainingQueryParser: Parser<ValueNode<QueryParameter.Remainder>> = MappedParser(
        LiteralTokenParser("{...}")
    ) {
        QueryParameter.Remainder
    }

    private val queryParameterParser: Parser<ValueNode<QueryParameter>> = MappedParser(
        ExactChoiceParser(
            staticQueryParser,
            parameterQueryParser,
            remainingQueryParser,
        )
    ) { choiceNode ->
        when (choiceNode) {
            is Choice3Node.Option1 -> choiceNode.node.value
            is Choice3Node.Option2 -> choiceNode.node.value
            is Choice3Node.Option3 -> choiceNode.node.value
        }
    }

    internal fun parseQueryParameter(query: String): QueryParameter {
        return queryParameterParser.parse(ParserContext.fromString(query)).first.value
    }

// Query String
// ---------------------------------------------------------------------------------------------------------------------

    internal val queryStringParser: Parser<ValueNode<List<QueryParameter>>> = MappedParser(
        SeparatedByParser(
            term = queryParameterParser,
            separator = CharInParser('&'),
        )
    ) {
        it.nodeList.map { it.value }
    }

    internal fun parseQueryString(queryString: String): List<QueryParameter> {
        return queryStringParser.parse(ParserContext.fromString(queryString)).first.value
    }

}
