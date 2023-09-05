/*
 * The code in this file was taken from Kermit and adapted for use in Ballast. See the following file for original
 * Kermit code:
 *
 * https://github.com/touchlab/Kermit/blob/5d47504cab34ec96e022da4f6df2d89f4f9d754a/kermit/src/darwinMain/kotlin/co/touchlab/kermit/OSLogWriter.kt
 *
 * Copyright (c) 2022 Touchlab
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.copperleaf.ballast.core

import com.copperleaf.ballast.BallastLogger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ptr
import platform.darwin.OS_LOG_DEFAULT
import platform.darwin.OS_LOG_TYPE_DEBUG
import platform.darwin.OS_LOG_TYPE_ERROR
import platform.darwin.OS_LOG_TYPE_INFO
import platform.darwin.__dso_handle
import platform.darwin._os_log_internal
import kotlin.experimental.ExperimentalNativeApi

/**
 * An implementation of a [BallastLogger] which writes log messages to iOS `OSLog`.
 */
@ExperimentalNativeApi
@ExperimentalForeignApi
public class OSLogLogger(private val tag: String? = null) : BallastLogger {
    override fun debug(message: String) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_DEBUG,
            "%s",
            formatMessage(tag, message),
        )
    }

    override fun info(message: String) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_INFO,
            "%s",
            formatMessage(tag, message),
        )
    }

    override fun error(throwable: Throwable) {
        _os_log_internal(
            __dso_handle.ptr,
            OS_LOG_DEFAULT,
            OS_LOG_TYPE_ERROR,
            "%s",
            formatMessage(tag, throwable.getStackTrace().joinToString("\n")),
        )
    }
}
