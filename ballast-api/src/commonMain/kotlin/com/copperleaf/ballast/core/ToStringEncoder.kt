package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastEncoder

public class ToStringEncoder<Inputs : Any, Events : Any, State : Any> : BallastEncoder<Inputs, Events, State> {
    override fun encodeInputToString(input: Inputs): String = input.toString()
    override fun encodeEventToString(event: Events): String = event.toString()
    override fun encodeStateToString(state: State): String = state.toString()
}
