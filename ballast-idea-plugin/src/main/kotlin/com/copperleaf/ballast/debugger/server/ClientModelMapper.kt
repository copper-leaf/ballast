package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.debugger.models.BallastDebuggerAction
import com.copperleaf.ballast.debugger.models.BallastDebuggerEvent
import com.copperleaf.ballast.debugger.server.unsupported.ClientModelMapperUnsupportedVersion
import com.copperleaf.ballast.debugger.server.v1.ClientModelMapperV1
import com.copperleaf.ballast.debugger.server.v2.ClientModelMapperV2

interface ClientModelMapper {

    val supported: Boolean

    fun mapIncoming(incoming: String): BallastDebuggerEvent

    fun mapOutgoing(outgoing: BallastDebuggerAction): String

    @Suppress("NOTHING_TO_INLINE")
    public data class Version(val major: Int, val minor: Int?, val patch: Int?) {
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
            return matchesMajor(other) && matchesMinor(other) && matchesPatch(other)
        }

        companion object {
            fun parse(connectionBallastVersion: String): Version {
                return try {
                    val (major, minor, patch) = connectionBallastVersion
                        .removeSuffix("-SNAPSHOT")
                        .split('.')
                        .map { it.trim().toInt() }

                    Version(major, minor, patch)
                } catch (e: Exception) {
                    Version(-1, -1, -1)
                }
            }
        }
    }

    companion object {
        private val supportedClientVersions = listOf(
            Version(1, null, null) to { ClientModelMapperV1() },
            Version(2, null, null) to { ClientModelMapperV2() },
        )

        fun getForVersion(
            connectionBallastVersion: String,
        ): ClientModelMapper {
            val version = Version.parse(connectionBallastVersion)

            return supportedClientVersions
                .firstOrNull { it.first.matches(version) }
                ?.second?.invoke()
                ?: ClientModelMapperUnsupportedVersion(connectionBallastVersion)
        }
    }
}
