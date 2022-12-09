package com.copperleaf.ballast.navigation.internal

import com.copperleaf.ballast.navigation.routing.PathSegment
import com.copperleaf.kudzu.checkNotEmpty
import com.copperleaf.kudzu.node.NodeContext
import com.copperleaf.kudzu.node.chars.CharNode
import com.copperleaf.kudzu.node.choice.Choice2Node
import com.copperleaf.kudzu.node.choice.Choice4Node
import com.copperleaf.kudzu.node.mapped.ValueNode
import com.copperleaf.kudzu.node.text.TextNode
import com.copperleaf.kudzu.parser.ParseFunction
import com.copperleaf.kudzu.parser.Parser
import com.copperleaf.kudzu.parser.ParserContext
import com.copperleaf.kudzu.parser.ParserException
import com.copperleaf.kudzu.parser.chars.CharInParser
import com.copperleaf.kudzu.parser.choice.ExactChoiceParser
import com.copperleaf.kudzu.parser.many.ManyParser
import com.copperleaf.kudzu.parser.many.SeparatedByParser
import com.copperleaf.kudzu.parser.mapped.FlatMappedParser
import com.copperleaf.kudzu.parser.mapped.MappedParser
import com.copperleaf.kudzu.parser.maybe.MaybeParser
import com.copperleaf.kudzu.parser.runParser
import com.copperleaf.kudzu.parser.sequence.SequenceParser
import com.copperleaf.kudzu.parser.text.IdentifierTokenParser
import com.copperleaf.kudzu.parser.text.LiteralTokenParser

internal object PathParser {

// Path Segments
// ---------------------------------------------------------------------------------------------------------------------

    private val pathSegmentTextParser: Parser<TextNode> = FlatMappedParser(
        SequenceParser(
            IdentifierTokenParser(),
            MaybeParser(
                ManyParser(
                    SequenceParser(
                        CharInParser('-', '_'),
                        IdentifierTokenParser()
                    )
                )
            )
        )
    ) {
        TextNode(it.text, it.context)
    }

    private val staticSegmentParser: Parser<ValueNode<PathSegment.Static>> = MappedParser(pathSegmentTextParser) {
        PathSegment.Static(it.text)
    }

    private val wildcardSegmentParser: Parser<ValueNode<PathSegment.Wildcard>> = MappedParser(CharInParser('*')) {
        PathSegment.Wildcard
    }

    private val parameterSegmentParser: Parser<ValueNode<PathSegment.Parameter>> = MappedParser(
        ExactChoiceParser(
            SequenceParser(
                CharInParser(':'),
                IdentifierTokenParser()
            ),
            SequenceParser(
                CharInParser('{'),
                IdentifierTokenParser(),
                MaybeParser(CharInParser('?')),
                CharInParser('}'),
            ),
        )
    ) { choiceNode ->
        when (choiceNode) {
            is Choice2Node.Option1 -> {
                PathSegment.Parameter(
                    name = choiceNode.node.node2.text,
                    optional = false
                )
            }

            is Choice2Node.Option2 -> {
                PathSegment.Parameter(
                    name = choiceNode.node.node2.text,
                    optional = choiceNode.node.node3.node != null
                )
            }
        }
    }

    private val tailcardSegmentParser: Parser<ValueNode<PathSegment.Tailcard>> = MappedParser(
        SequenceParser(
            CharInParser('{'),
            MaybeParser(IdentifierTokenParser()),
            LiteralTokenParser("..."),
            CharInParser('}'),
        ),
    ) { sequenceNode ->
        PathSegment.Tailcard(
            name = sequenceNode.node2.node?.text
        )
    }

    private val segmentParser: Parser<ValueNode<PathSegment>> = MappedParser(
        ExactChoiceParser(
            staticSegmentParser,
            wildcardSegmentParser,
            parameterSegmentParser,
            tailcardSegmentParser,
        )
    ) { choiceNode ->
        when (choiceNode) {
            is Choice4Node.Option1 -> choiceNode.node.value
            is Choice4Node.Option2 -> choiceNode.node.value
            is Choice4Node.Option3 -> choiceNode.node.value
            is Choice4Node.Option4 -> choiceNode.node.value
        }
    }

    internal fun parsePathSegment(segment: String): PathSegment {
        return segmentParser.parse(ParserContext.fromString(segment)).first.value
    }

// Full /-separated path
// ---------------------------------------------------------------------------------------------------------------------

    private class LeadingSlashParser : Parser<CharNode> {
        override fun predict(input: ParserContext): Boolean {
            return input.validateNextChar { it == '/' }
        }

        override val parse: ParseFunction<CharNode> = runParser { input ->
            checkNotEmpty(input)

            val (nextChar, remaining) = input.nextChar()

            if (nextChar != '/') throw ParserException(
                "Path must start with a leading slash",
                this@LeadingSlashParser,
                input
            )

            CharNode(nextChar, NodeContext(input, remaining)) to remaining
        }
    }

    internal val pathParser: Parser<ValueNode<List<PathSegment>>> = MappedParser(
        SequenceParser(
            LeadingSlashParser(),
            MaybeParser(
                SeparatedByParser(
                    term = segmentParser,
                    separator = CharInParser('/'),
                )
            )
        )
    ) { (_, _, segments) ->
        segments.node?.nodeList?.map { it.value } ?: emptyList()
    }

    internal fun parsePath(path: String): List<PathSegment> {
        return pathParser.parse(ParserContext.fromString(path)).first.value
    }
}
