package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.debugger.server.Assertions.assertEquals
import com.copperleaf.ballast.debugger.versions.ClientVersion
import com.copperleaf.ballast.debugger.versions.CompositeModelSerializer
import com.copperleaf.ballast.debugger.versions.unsupported.ClientModelMapperUnsupportedVersion
import com.copperleaf.ballast.debugger.versions.v4.ClientModelSerializerV4
import io.kotest.core.spec.style.StringSpec

class ClientModelMapperTest : StringSpec({
    "testParsingVersions" {
        assertEquals(ClientVersion(-1, -1, -1), ClientVersion.parse("some.unsupported.format"))
        assertEquals(ClientVersion(-1, -1, -1), ClientVersion.parse("some unsupported format"))
        assertEquals(ClientVersion(0, 0, 0), ClientVersion.parse("0.0.0"))
        assertEquals(ClientVersion(0, 1, 0), ClientVersion.parse("0.1.0"))
        assertEquals(ClientVersion(0, 1, 1), ClientVersion.parse("0.1.1"))
        assertEquals(ClientVersion(1, 1, 0), ClientVersion.parse("1.1.0"))
        assertEquals(ClientVersion(2, 3, 0), ClientVersion.parse("2.3.0"))
        assertEquals(ClientVersion(2, 3, 1), ClientVersion.parse("2.3.1"))
        assertEquals(ClientVersion(3, 0, 0), ClientVersion.parse("3.0.0"))
        assertEquals(ClientVersion(4, 0, 0), ClientVersion.parse("4.0.0"))

        assertEquals(ClientVersion(0, 0, 0), ClientVersion.parse("0.0.0-SNAPSHOT"))
        assertEquals(ClientVersion(0, 1, 0), ClientVersion.parse("0.1.0-SNAPSHOT"))
        assertEquals(ClientVersion(0, 1, 1), ClientVersion.parse("0.1.1-SNAPSHOT"))
        assertEquals(ClientVersion(1, 1, 0), ClientVersion.parse("1.1.0-SNAPSHOT"))
        assertEquals(ClientVersion(2, 3, 0), ClientVersion.parse("2.3.0-SNAPSHOT"))
        assertEquals(ClientVersion(2, 3, 1), ClientVersion.parse("2.3.1-SNAPSHOT"))
        assertEquals(ClientVersion(3, 0, 0), ClientVersion.parse("3.0.0-SNAPSHOT"))
        assertEquals(ClientVersion(4, 0, 0), ClientVersion.parse("4.0.0-SNAPSHOT"))
    }

    "testIsSupported" {
        assertEquals(false, ClientVersion.getSerializer("some.unsupported.format").supported)
        assertEquals(false, ClientVersion.getSerializer("some unsupported format").supported)
        assertEquals(false, ClientVersion.getSerializer("0.1.0").supported)
        assertEquals(false, ClientVersion.getSerializer("0.1.1").supported)
        assertEquals(true, ClientVersion.getSerializer("1.1.0").supported)
        assertEquals(true, ClientVersion.getSerializer("2.3.0").supported)
        assertEquals(true, ClientVersion.getSerializer("2.3.1").supported)
        assertEquals(true, ClientVersion.getSerializer("3.0.0").supported)
        assertEquals(true, ClientVersion.getSerializer("3.2.0").supported)
        assertEquals(true, ClientVersion.getSerializer("3.2.2").supported)
        assertEquals(true, ClientVersion.getSerializer("4.0.0").supported)
        assertEquals(true, ClientVersion.getSerializer("4.0.1").supported)
        assertEquals(true, ClientVersion.getSerializer("4.1.0").supported)
        assertEquals(true, ClientVersion.getSerializer("4.1.1").supported)
        assertEquals(false, ClientVersion.getSerializer("5.0.0").supported)

        assertEquals(false, ClientVersion.getSerializer("0.1.0-SNAPSHOT").supported)
        assertEquals(false, ClientVersion.getSerializer("0.1.1-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("1.1.0-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("2.3.0-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("2.3.1-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("3.0.0-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("3.2.0-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("3.2.2-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("4.0.0-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("4.0.1-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("4.1.0-SNAPSHOT").supported)
        assertEquals(true, ClientVersion.getSerializer("4.1.1-SNAPSHOT").supported)
        assertEquals(false, ClientVersion.getSerializer("5.0.0-SNAPSHOT").supported)
    }

    "testMatchingClientVersionsToMappers" {
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("some.unsupported.format")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("some unsupported format")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("0.1.0")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("0.1.1")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("1.1.0")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("2.3.0")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("2.3.1")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("3.0.0")::class)
        assertEquals(ClientModelSerializerV4::class, ClientVersion.getSerializer("4.0.0")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("5.0.0")::class)

        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("0.1.0-SNAPSHOT")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("0.1.1-SNAPSHOT")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("1.1.0-SNAPSHOT")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("2.3.0-SNAPSHOT")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("2.3.1-SNAPSHOT")::class)
        assertEquals(CompositeModelSerializer::class, ClientVersion.getSerializer("3.0.0-SNAPSHOT")::class)
        assertEquals(ClientModelSerializerV4::class, ClientVersion.getSerializer("4.0.0-SNAPSHOT")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientVersion.getSerializer("5.0.0-SNAPSHOT")::class)
    }

    "testVersionMatching" {
        ClientVersion(1, 0, 0).apply {
            assertEquals(true, matches(ClientVersion(1, 0, 0)))
            assertEquals(false, matches(ClientVersion(1, 0, 1)))
            assertEquals(false, matches(ClientVersion(1, 1, 0)))
            assertEquals(false, matches(ClientVersion(1, 1, 1)))
            assertEquals(false, matches(ClientVersion(2, 0, 0)))
            assertEquals(false, matches(ClientVersion(0, 1, 0)))
        }

        ClientVersion(1, 0, null).apply {
            assertEquals(true, matches(ClientVersion(1, 0, 0)))
            assertEquals(true, matches(ClientVersion(1, 0, 1)))
            assertEquals(true, matches(ClientVersion(1, 0, 2)))
            assertEquals(false, matches(ClientVersion(1, 1, 0)))
            assertEquals(false, matches(ClientVersion(1, 1, 1)))
            assertEquals(false, matches(ClientVersion(2, 0, 0)))
            assertEquals(false, matches(ClientVersion(0, 1, 0)))
        }

        ClientVersion(1, null, 0).apply {
            assertEquals(true, matches(ClientVersion(1, 0, 0)))
            assertEquals(false, matches(ClientVersion(1, 0, 1)))
            assertEquals(true, matches(ClientVersion(1, 1, 0)))
            assertEquals(true, matches(ClientVersion(1, 2, 0)))
            assertEquals(false, matches(ClientVersion(1, 1, 1)))
            assertEquals(false, matches(ClientVersion(2, 0, 0)))
            assertEquals(false, matches(ClientVersion(0, 1, 0)))
        }

        ClientVersion(1, null, null).apply {
            assertEquals(true, matches(ClientVersion(1, 0, 0)))
            assertEquals(true, matches(ClientVersion(1, 0, 1)))
            assertEquals(true, matches(ClientVersion(1, 0, 2)))
            assertEquals(true, matches(ClientVersion(1, 1, 0)))
            assertEquals(true, matches(ClientVersion(1, 2, 0)))
            assertEquals(true, matches(ClientVersion(1, 1, 1)))
            assertEquals(true, matches(ClientVersion(1, 2, 1)))
            assertEquals(false, matches(ClientVersion(2, 0, 0)))
            assertEquals(false, matches(ClientVersion(0, 1, 0)))
        }
    }
})
