package com.copperleaf.ballast.crashreporting

public interface CrashReporter {

    public fun logInput(viewModelName: String, input: Any)

    public fun recordInputError(
        viewModelName: String,
        input: Any,
        throwable: Throwable,
    )

    public fun recordEventError(
        viewModelName: String,
        event: Any,
        throwable: Throwable,
    )

    public fun recordSideJobError(
        viewModelName: String,
        key: String,
        throwable: Throwable,
    )

    public fun recordUnhandledError(
        viewModelName: String,
        throwable: Throwable,
    )

}
