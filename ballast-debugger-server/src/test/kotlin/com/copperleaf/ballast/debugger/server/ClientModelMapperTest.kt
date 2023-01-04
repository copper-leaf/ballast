package com.copperleaf.ballast.debugger.server

import com.copperleaf.ballast.debugger.server.versions.unsupported.ClientModelMapperUnsupportedVersion
import com.copperleaf.ballast.debugger.server.versions.v1.ClientModelMapperV1
import com.copperleaf.ballast.debugger.server.versions.v2.ClientModelMapperV2
import org.junit.Assert.assertEquals
import org.junit.Test

class ClientModelMapperTest {

    @Test
    fun testParsingVersions() {
        assertEquals(ClientModelMapper.Version(-1, -1, -1), ClientModelMapper.Version.parse("some.unsupported.format"))
        assertEquals(ClientModelMapper.Version(-1, -1, -1), ClientModelMapper.Version.parse("some unsupported format"))
        assertEquals(ClientModelMapper.Version(0, 0, 0), ClientModelMapper.Version.parse("0.0.0"))
        assertEquals(ClientModelMapper.Version(0, 1, 0), ClientModelMapper.Version.parse("0.1.0"))
        assertEquals(ClientModelMapper.Version(0, 1, 1), ClientModelMapper.Version.parse("0.1.1"))
        assertEquals(ClientModelMapper.Version(1, 1, 0), ClientModelMapper.Version.parse("1.1.0"))
        assertEquals(ClientModelMapper.Version(2, 3, 0), ClientModelMapper.Version.parse("2.3.0"))
        assertEquals(ClientModelMapper.Version(2, 3, 1), ClientModelMapper.Version.parse("2.3.1"))
        assertEquals(ClientModelMapper.Version(3, 0, 0), ClientModelMapper.Version.parse("3.0.0"))

        assertEquals(ClientModelMapper.Version(0, 0, 0), ClientModelMapper.Version.parse("0.0.0-SNAPSHOT"))
        assertEquals(ClientModelMapper.Version(0, 1, 0), ClientModelMapper.Version.parse("0.1.0-SNAPSHOT"))
        assertEquals(ClientModelMapper.Version(0, 1, 1), ClientModelMapper.Version.parse("0.1.1-SNAPSHOT"))
        assertEquals(ClientModelMapper.Version(1, 1, 0), ClientModelMapper.Version.parse("1.1.0-SNAPSHOT"))
        assertEquals(ClientModelMapper.Version(2, 3, 0), ClientModelMapper.Version.parse("2.3.0-SNAPSHOT"))
        assertEquals(ClientModelMapper.Version(2, 3, 1), ClientModelMapper.Version.parse("2.3.1-SNAPSHOT"))
        assertEquals(ClientModelMapper.Version(3, 0, 0), ClientModelMapper.Version.parse("3.0.0-SNAPSHOT"))
    }

    @Test
    fun testIsSupported() {
        assertEquals(false, ClientModelMapper.getForVersion("some.unsupported.format").supported)
        assertEquals(false, ClientModelMapper.getForVersion("some unsupported format").supported)
        assertEquals(false, ClientModelMapper.getForVersion("0.1.0").supported)
        assertEquals(false, ClientModelMapper.getForVersion("0.1.1").supported)
        assertEquals(true, ClientModelMapper.getForVersion("1.1.0").supported)
        assertEquals(true, ClientModelMapper.getForVersion("2.3.0").supported)
        assertEquals(true, ClientModelMapper.getForVersion("2.3.1").supported)
        assertEquals(false, ClientModelMapper.getForVersion("3.0.0").supported)

        assertEquals(false, ClientModelMapper.getForVersion("0.1.0-SNAPSHOT").supported)
        assertEquals(false, ClientModelMapper.getForVersion("0.1.1-SNAPSHOT").supported)
        assertEquals(true, ClientModelMapper.getForVersion("1.1.0-SNAPSHOT").supported)
        assertEquals(true, ClientModelMapper.getForVersion("2.3.0-SNAPSHOT").supported)
        assertEquals(true, ClientModelMapper.getForVersion("2.3.1-SNAPSHOT").supported)
        assertEquals(false, ClientModelMapper.getForVersion("3.0.0-SNAPSHOT").supported)
    }

    @Test
    fun testMatchingClientVersionsToMappers() {
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("some.unsupported.format")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("some unsupported format")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("0.1.0")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("0.1.1")::class)
        assertEquals(ClientModelMapperV1::class, ClientModelMapper.getForVersion("1.1.0")::class)
        assertEquals(ClientModelMapperV2::class, ClientModelMapper.getForVersion("2.3.0")::class)
        assertEquals(ClientModelMapperV2::class, ClientModelMapper.getForVersion("2.3.1")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("3.0.0")::class)

        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("0.1.0-SNAPSHOT")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("0.1.1-SNAPSHOT")::class)
        assertEquals(ClientModelMapperV1::class, ClientModelMapper.getForVersion("1.1.0-SNAPSHOT")::class)
        assertEquals(ClientModelMapperV2::class, ClientModelMapper.getForVersion("2.3.0-SNAPSHOT")::class)
        assertEquals(ClientModelMapperV2::class, ClientModelMapper.getForVersion("2.3.1-SNAPSHOT")::class)
        assertEquals(ClientModelMapperUnsupportedVersion::class, ClientModelMapper.getForVersion("3.0.0-SNAPSHOT")::class)
    }

    @Test
    fun testVersionMatching() {
        ClientModelMapper.Version(1, 0, 0).apply {
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 0, 1)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 1, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 1, 1)))
            assertEquals(false, matches(ClientModelMapper.Version(2, 0, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(0, 1, 0)))
        }

        ClientModelMapper.Version(1, 0, null).apply {
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 0)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 1)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 2)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 1, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 1, 1)))
            assertEquals(false, matches(ClientModelMapper.Version(2, 0, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(0, 1, 0)))
        }

        ClientModelMapper.Version(1, null, 0).apply {
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 0, 1)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 1, 0)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 2, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(1, 1, 1)))
            assertEquals(false, matches(ClientModelMapper.Version(2, 0, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(0, 1, 0)))
        }

        ClientModelMapper.Version(1, null, null).apply {
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 0)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 1)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 0, 2)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 1, 0)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 2, 0)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 1, 1)))
            assertEquals(true, matches(ClientModelMapper.Version(1, 2, 1)))
            assertEquals(false, matches(ClientModelMapper.Version(2, 0, 0)))
            assertEquals(false, matches(ClientModelMapper.Version(0, 1, 0)))
        }
    }
}
