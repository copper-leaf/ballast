package com.copperleaf.ballast

import com.copperleaf.ballast.impl.TestContract

class TestInputFilter : InputFilter<
    TestContract.Inputs,
    TestContract.Events,
    TestContract.State> {
    override fun filterInput(state: TestContract.State, input: TestContract.Inputs): InputFilter.Result = when (input) {
        is TestContract.Inputs.FilteredValue -> InputFilter.Result.Reject
        else -> InputFilter.Result.Accept
    }
}
