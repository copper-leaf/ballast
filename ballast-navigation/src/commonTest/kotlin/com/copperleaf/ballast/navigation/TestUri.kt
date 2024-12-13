package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.UriBuilder
import com.copperleaf.ballast.navigation.internal.UriDecoder
import com.copperleaf.ballast.navigation.internal.UriEncoder
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestUri {
    @Test
    fun uriEncoder_encodeURLPathPart() = runTest {
        fun testPathEncodings(decoded: String, encoded: String) {
            assertEquals<Any?>(encoded, UriEncoder.encodeUrlPathSegment(decoded))
            assertEquals<Any?>(decoded, UriDecoder.decodeUrlPathSegment(encoded))
        }
        testPathEncodings("path", "path")
        testPathEncodings("/path", "%2Fpath")
        testPathEncodings("this is my input.", "this%20is%20my%20input.")
    }

    @Test
    fun uriEncoder_encodeURLQueryComponent() = runTest {
        assertEquals<Any?>("path", UriEncoder.encodeUrlQueryString("path"))
        assertEquals<Any?>("/path", UriEncoder.encodeUrlQueryString("/path"))
        assertEquals<Any?>("this%20is%20my%20input.", UriEncoder.encodeUrlQueryString("this is my input."))
        assertEquals<Any?>("one=two", UriEncoder.encodeUrlQueryString("one=two"))
        assertEquals<Any?>("one=two&three=four", UriEncoder.encodeUrlQueryString("one=two&three=four"))
        assertEquals<Any?>("?one=two&three=four", UriEncoder.encodeUrlQueryString("?one=two&three=four"))

        assertEquals<Any?>("path", UriEncoder.encodeUrlQueryString("path", spaceToPlus = true))
        assertEquals<Any?>("/path", UriEncoder.encodeUrlQueryString("/path", spaceToPlus = true))
        assertEquals<Any?>(
            "this+is+my+input.",
            UriEncoder.encodeUrlQueryString("this is my input.", spaceToPlus = true)
        )
        assertEquals<Any?>("one=two", UriEncoder.encodeUrlQueryString("one=two", spaceToPlus = true))
        assertEquals<Any?>(
            "one=two&three=four",
            UriEncoder.encodeUrlQueryString("one=two&three=four", spaceToPlus = true)
        )
        assertEquals<Any?>(
            "?one=two&three=four",
            UriEncoder.encodeUrlQueryString("?one=two&three=four", spaceToPlus = true)
        )

        assertEquals<Any?>("path", UriEncoder.encodeUrlQueryComponent("path"))
        assertEquals<Any?>("%2Fpath", UriEncoder.encodeUrlQueryComponent("/path"))
        assertEquals<Any?>("this%20is%20my%20input%2E", UriEncoder.encodeUrlQueryComponent("this is my input."))
        assertEquals<Any?>("one%3Dtwo", UriEncoder.encodeUrlQueryComponent("one=two"))
        assertEquals<Any?>("one%3Dtwo%26three%3Dfour", UriEncoder.encodeUrlQueryComponent("one=two&three=four"))
        assertEquals<Any?>("%3Fone%3Dtwo%26three%3Dfour", UriEncoder.encodeUrlQueryComponent("?one=two&three=four"))

        assertEquals<Any?>("path", UriEncoder.encodeUrlQueryComponent("path", spaceToPlus = true))
        assertEquals<Any?>("%2Fpath", UriEncoder.encodeUrlQueryComponent("/path", spaceToPlus = true))
        assertEquals<Any?>(
            "this+is+my+input%2E",
            UriEncoder.encodeUrlQueryComponent("this is my input.", spaceToPlus = true)
        )
        assertEquals<Any?>("one%3Dtwo", UriEncoder.encodeUrlQueryComponent("one=two", spaceToPlus = true))
        assertEquals<Any?>(
            "one%3Dtwo%26three%3Dfour",
            UriEncoder.encodeUrlQueryComponent("one=two&three=four", spaceToPlus = true)
        )
        assertEquals<Any?>(
            "%3Fone%3Dtwo%26three%3Dfour", UriEncoder.encodeUrlQueryComponent(
                "?one=two&three=four",
                spaceToPlus = true
            )
        )
    }

    @Test
    fun uri_parse_withNoHostInfo() = runTest {
        val uri = UriBuilder.parse("/one/two/and three?a=b&c=d&c=e&key with spaces=value with spaces#/my/doc/fragment")
        assertEquals<Any?>("http", uri.protocol)
        //        uri.port shouldBe 80
        assertEquals<Any?>("localhost", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key with spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertEquals<Any?>("/my/doc/fragment", uri.decodedFragment)
        //        uri.toString() shouldBe "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment"
    }

    @Test
    fun uri_parse() = runTest {
        val uri =
            UriBuilder.parse("https://www.example.com/one/two/and three?a=b&c=d&c=e&key with spaces=value with spaces#/my/doc/fragment")
        assertEquals<Any?>("https", uri.protocol)
        assertEquals<Any?>(443, uri.port)
        assertEquals<Any?>("www.example.com", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key with spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertEquals<Any?>("/my/doc/fragment", uri.decodedFragment)
        assertEquals<Any?>(
            "https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment",
            uri.toString()
        )
    }

    @Test
    fun uri_parse_withEncodedPathAndQueryParameters() = runTest {
        val uri =
            UriBuilder.parse("https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment")
        assertEquals<Any?>("https", uri.protocol)
        assertEquals<Any?>(443, uri.port)
        assertEquals<Any?>("www.example.com", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key with spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertEquals<Any?>("/my/doc/fragment", uri.decodedFragment)
        assertEquals<Any?>(
            "https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment",
            uri.toString()
        )
    }

    @Test
    fun uri_parse_withEncodedPathAndQueryParametersUsingSpaceAsPlus() = runTest {
        val uri =
            UriBuilder.parse("https://www.example.com/one/two/and+three?a=b&c=d&c=e&key+with+spaces=value+with+spaces#/my/doc/fragment")
        assertEquals<Any?>("https", uri.protocol)
        assertEquals<Any?>(443, uri.port)
        assertEquals<Any?>("www.example.com", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and+three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key+with+spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertEquals<Any?>("/my/doc/fragment", uri.decodedFragment)
        assertEquals<Any?>(
            "https://www.example.com/one/two/and+three?a=b&c=d&c=e&key%2Bwith%2Bspaces=value%20with%20spaces#/my/doc/fragment",
            uri.toString()
        )
    }

    @Test
    fun uri_build() = runTest {
        val uri = UriBuilder.build(
            encodedPath = "/one/two/and three",
            encodedQueryString = "a=b&c=d&c=e&key with spaces=value with spaces",
        )
        assertEquals<Any?>("http", uri.protocol)
        //        uri.port shouldBe 80 // disabled because the default JS port is 9876 since it's running on a local webserver
        assertEquals<Any?>("localhost", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key with spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertNull(uri.decodedFragment)
        assertEquals<Any?>(
            "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces",
            uri.toString()
        )
    }

    @Test
    fun uri_build_withEncodedPathAndQueryParametersUsingSpaceAsPlus() = runTest {
        val uri = UriBuilder.build(
            encodedPath = "/one/two/and+three",
            encodedQueryString = "a=b&c=d&c=e&key+with+spaces=value+with+spaces",
        )
        assertEquals<Any?>("http", uri.protocol)
        assertEquals<Any?>(80, uri.port)
        assertEquals<Any?>("localhost", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and+three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key+with+spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertNull(uri.decodedFragment)
        assertEquals<Any?>(
            "http://localhost/one/two/and+three?a=b&c=d&c=e&key%2Bwith%2Bspaces=value%20with%20spaces",
            uri.toString()
        )
    }

    @Test
    fun uri_build_withEncodedPathAndQueryParameters() = runTest {
        val uri = UriBuilder.build(
            encodedPath = "/one/two/and%20three",
            encodedQueryString = "a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces",
        )
        assertEquals<Any?>("http", uri.protocol)
        assertEquals<Any?>(80, uri.port)
        assertEquals<Any?>("localhost", uri.host)
        assertEquals<Any?>(listOf("one", "two", "and three"), uri.decodedPathSegments)
        assertEquals<Any?>(
            mapOf(
                "a" to listOf("b"),
                "c" to listOf("d", "e"),
                "key with spaces" to listOf("value with spaces")
            ), uri.decodedQueryParameters
        )
        assertNull(uri.decodedFragment)
        assertEquals<Any?>(
            "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces",
            uri.toString()
        )
    }
}
