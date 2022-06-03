package com.copperleaf.ballast

import kotlinx.coroutines.channels.Channel

/**
 * An interface for handling Inputs sent to the ViewModel. Inputs sent to the ViewModel are buffered into a [Channel],
 * and then read from the channel and sent to the [InputHandler] by the [InputStrategy] provided to the
 * [BallastViewModelConfiguration] ([BallastViewModelConfiguration.inputStrategy]). Any guarantees around how/when an
 * Input is delivered here is provided by the [InputStrategy], so refer to the documentation there to fully understand
 * how your Inputs are being scheduled and processed. Inputs will be run on the Dispatcher provided to the
 * [BallastViewModelConfiguration] ([BallastViewModelConfiguration.inputsDispatcher]).
 */
public interface InputHandler<Inputs : Any, Events : Any, State : Any> {

    /**
     * Asynchronously handle an Input. Prefer using a sealed class to describe all Inputs, and a `when` expression to
     * ensure you're handling all inputs. This method will be running on the Dispatcher provided to the
     * [BallastViewModelConfiguration] ([BallastViewModelConfiguration.inputsDispatcher]).
     */
    public suspend fun InputHandlerScope<Inputs, Events, State>.handleInput(
        input: Inputs
    )
}
