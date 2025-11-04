package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.UriBuilder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TestUriBuilder {
    @Test
    fun uriBuilder_base() = runTest {
        val uri = UriBuilder.build(
            encodedPath = "/app/ballast",
            encodedQueryString = "one=1&two=2",
        )
        assertEquals("/app/ballast?one=1&two=2", uri.encodedPathAndQuery)
        assertEquals("/app/ballast?one=1&two=2", UriBuilder.parse(uri.encodedPathAndQuery).encodedPathAndQuery)
    }

    @Test
    fun uriBuilder_withSlashBasePath() = runTest {
        val url = UriBuilder.build(
            encodedPath = "/app/ballast",
            encodedQueryString = "one=1&two=2",
        )
        val basePath = "/"
        val updatedUrl = if(basePath != null) {
            UriBuilder.build(
                encodedPath = "${basePath}/${url.encodedPath.trim('/')}",
                encodedQueryString = url.encodedQueryString.trimStart('?'),
            )
        } else {
            url
        }

        assertEquals("/app/ballast?one=1&two=2", updatedUrl.encodedPathAndQuery)
        assertEquals("/app/ballast?one=1&two=2", UriBuilder.parse(updatedUrl.encodedPathAndQuery).encodedPathAndQuery)
    }

    @Test
    fun uriBuilder_withFullBasePath() = runTest {
        val url = UriBuilder.build(
            encodedPath = "/app/ballast",
            encodedQueryString = "one=1&two=2",
        )
        val basePath = "/one/two/three"
        val updatedUrl = if(basePath != null) {
            UriBuilder.build(
                encodedPath = "${basePath}/${url.encodedPath.trim('/')}",
                encodedQueryString = url.encodedQueryString.trimStart('?'),
            )
        } else {
            url
        }

        assertEquals("/one/two/three/app/ballast?one=1&two=2", updatedUrl.encodedPathAndQuery)
        assertEquals("/one/two/three/app/ballast?one=1&two=2", UriBuilder.parse(updatedUrl.encodedPathAndQuery).encodedPathAndQuery)
    }

    @Test
    fun uriBuilder_withNullBasePath() = runTest {
        val url = UriBuilder.build(
            encodedPath = "/app/ballast",
            encodedQueryString = "one=1&two=2",
        )
        val basePath = null
        val updatedUrl = if(basePath != null) {
            UriBuilder.build(
                encodedPath = "${basePath}/${url.encodedPath.trim('/')}",
                encodedQueryString = url.encodedQueryString.trimStart('?'),
            )
        } else {
            url
        }

        assertEquals("/app/ballast?one=1&two=2", updatedUrl.encodedPathAndQuery)
        assertEquals("/app/ballast?one=1&two=2", UriBuilder.parse(updatedUrl.encodedPathAndQuery).encodedPathAndQuery)
    }

    @Test
    fun uriBuilder_badQueryString() = runTest {
        val url = UriBuilder.build(
            encodedPath = "/app/ballast",
            encodedQueryString = "one=1&two=2",
        )
        val basePath = "/"
        assertFails {
            if(basePath != null) {
                UriBuilder.build(
                    encodedPath = "${basePath}/${url.encodedPath.trim('/')}",
                    encodedQueryString = url.encodedQueryString,
                )
            } else {
                url
            }
        }
    }
}
