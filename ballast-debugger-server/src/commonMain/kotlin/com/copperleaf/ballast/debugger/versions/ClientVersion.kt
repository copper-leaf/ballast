package com.copperleaf.ballast.debugger.versions

import com.copperleaf.ballast.debugger.versions.unsupported.ClientModelMapperUnsupportedVersion
import com.copperleaf.ballast.debugger.versions.v1.ClientModelSerializerV1
import com.copperleaf.ballast.debugger.versions.v2.ClientModelConverterV1ToV2
import com.copperleaf.ballast.debugger.versions.v2.ClientModelSerializerV2
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerActionV4
import com.copperleaf.ballast.debugger.versions.v4.BallastDebuggerEventV4
import com.copperleaf.ballast.debugger.versions.v3.ClientModelConverterV2ToV3
import com.copperleaf.ballast.debugger.versions.v3.ClientModelSerializerV3
import com.copperleaf.ballast.debugger.versions.v4.ClientModelConverterV3ToV4
import com.copperleaf.ballast.debugger.versions.v4.ClientModelSerializerV4

@Suppress("NOTHING_TO_INLINE")
public data class ClientVersion(val major: Int, val minor: Int?, val patch: Int?) : Comparable<ClientVersion> {
    private inline fun matchesMajor(other: ClientVersion): Boolean {
        return this.major == other.major
    }

    private inline fun matchesMinor(other: ClientVersion): Boolean {
        return if (this.minor == null) {
            true
        } else {
            this.minor == other.minor
        }
    }

    private inline fun matchesPatch(other: ClientVersion): Boolean {
        return if (this.patch == null) {
            true
        } else {
            this.patch == other.patch
        }
    }

    public fun matches(other: ClientVersion): Boolean {
        return matchesMajor(other) && matchesMinor(other) && matchesPatch(other)
    }

    override fun compareTo(other: ClientVersion): Int {
        val comparator = compareBy<ClientVersion>(
            { it.major },
            { it.minor },
            { it.patch },
        )

        return comparator.compare(this, other)
    }

    override fun toString(): String {
        return buildString {
            append(major)

            append(".")
            if (minor != null) {
                append(minor)
            } else {
                append("X")
            }

            append(".")
            if (patch != null) {
                append(patch)
            } else {
                append("X")
            }
        }
    }


    public companion object {
        public fun parse(connectionBallastVersion: String): ClientVersion {
            return try {
                val (major, minor, patch) = connectionBallastVersion
                    .removeSuffix("-SNAPSHOT")
                    .split('.')
                    .map { it.trim().toInt() }

                ClientVersion(major, minor, patch)
            } catch (e: Exception) {
                ClientVersion(-1, -1, -1)
            }
        }

        public fun getSerializer(
            clientVersion: ClientVersion
        ): ClientModelSerializer<BallastDebuggerEventV4, BallastDebuggerActionV4> {
            return when (clientVersion.major) {
                1 -> CompositeModelSerializer(
                    serializer = ClientModelSerializerV1(),
                    converter = CompositeModelConverter(
                        CompositeModelConverter(
                            ClientModelConverterV1ToV2(),
                            ClientModelConverterV2ToV3(),
                        ),
                        ClientModelConverterV3ToV4(),
                    ),
                )

                2 -> CompositeModelSerializer(
                    serializer = ClientModelSerializerV2(),
                    converter = CompositeModelConverter(
                        ClientModelConverterV2ToV3(),
                        ClientModelConverterV3ToV4(),
                    ),
                )

                3 -> CompositeModelSerializer(
                    serializer = ClientModelSerializerV3(),
                    converter = ClientModelConverterV3ToV4(),
                )

                4 -> ClientModelSerializerV4()

                else -> ClientModelMapperUnsupportedVersion(clientVersion)
            }
        }

        public fun getSerializer(
            clientVersion: String
        ): ClientModelSerializer<BallastDebuggerEventV4, BallastDebuggerActionV4> {
            return getSerializer(parse(clientVersion))
        }
    }
}
