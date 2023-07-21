package com.copperleaf.ballast.contracts.test

import com.copperleaf.ballast.InputFilter

class TestInputFilter : InputFilter<
        TestContract.Inputs,
        TestContract.Events,
        TestContract.State> {
    override fun filterInput(state: TestContract.State, input: TestContract.Inputs): InputFilter.Result = when (input) {
        is TestContract.Inputs.FilteredValue -> InputFilter.Result.Reject
        else -> InputFilter.Result.Accept
    }
}
