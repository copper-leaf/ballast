package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.v2.ClientModelMapperV2

interface ClientModelMapper {

    fun mapIncoming(incoming: String): BallastDebuggerEvent

    fun mapOutgoing(outgoing: BallastDebuggerAction): String

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        private class Version(val major: Int, val minor: Int?, val patch: Int?) {
            private inline fun matchesMajor(other: Version): Boolean {
                return this.major == other.major
            }

            private inline fun matchesMinor(other: Version): Boolean {
                return if (this.minor == null) {
                    true
                } else {
                    this.minor == other.minor
                }
            }

            private inline fun matchesPatch(other: Version): Boolean {
                return if (this.patch == null) {
                    true
                } else {
                    this.patch == other.patch
                }
            }

            fun matches(other: Version): Boolean {
                val matchesMajor = matchesMajor(other)
                val matchesMinor = matchesMajor && matchesMinor(other)
                val matchesPatch = matchesMinor && matchesPatch(other)

                return matchesPatch
            }

            companion object {
                fun parse(connectionBallastVersion: String): Version {
                    val (major, minor, patch) = connectionBallastVersion
                        .split('.')
                        .map { it.trim().toIntOrNull() ?: 0 }

                    return Version(major, minor, patch)
                }
            }
        }


        private val supportedClientVersions = listOf(
            Version(1, null, null) to { ClientModelMapperV2() },
            Version(2, null, null) to { ClientModelMapperV2() },
        )

        fun isSupported(
            connectionBallastVersion: String,
        ): Boolean {
            return getForVersion(connectionBallastVersion) != null
        }

        fun getForVersion(
            connectionBallastVersion: String,
        ): ClientModelMapper? {
            val version = Version.parse(connectionBallastVersion)

            return supportedClientVersions
                .firstOrNull { it.first.matches(version) }
                ?.second?.invoke()
                ?: run {
                    println("Unsupported client version: $connectionBallastVersion")

                    null
                }
        }
    }
}
