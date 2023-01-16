/*
 * The code in this file was taken from Kermit and adapted for use in Ballast. See the following file for original
 * Kermit code:
 *
 * https://github.com/touchlab/Kermit/blob/5d47504cab34ec96e022da4f6df2d89f4f9d754a/kermit/src/darwinMain/kotlin/co/touchlab/kermit/NSLogWriter.kt
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
import platform.Foundation.NSLog
import platform.Foundation.NSString

/**
 * An implementation of a [BallastLogger] which writes log messages to iOS `NSLog`.
 */
@Suppress("CAST_NEVER_SUCCEEDS")
public class NSLogLogger(private val tag: String = "Ballast") : BallastLogger {
    override fun debug(message: String) {
        NSLog("%s: [%@] %@", "Debug", tag as NSString, message as NSString)
    }

    override fun info(message: String) {
        NSLog("%s: [%@] %@", "Info", tag as NSString, message as NSString)
    }

    override fun error(throwable: Throwable) {
        val string = throwable.stackTraceToString()
        NSLog("%@", string as NSString)
    }
}
