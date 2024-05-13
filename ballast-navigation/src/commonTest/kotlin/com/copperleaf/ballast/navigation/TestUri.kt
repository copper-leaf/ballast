package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.UriBuilder
import com.copperleaf.ballast.navigation.internal.UriDecoder
import com.copperleaf.ballast.navigation.internal.UriEncoder
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestUri : StringSpec({
    "UriEncoder.encodeURLPathPart()" {
        fun testPathEncodings(decoded: String, encoded: String) {
            UriEncoder.encodeUrlPathSegment(decoded) shouldBe encoded
            UriDecoder.decodeUrlPathSegment(encoded) shouldBe decoded
        }
        testPathEncodings("path", "path")
        testPathEncodings("/path", "%2Fpath")
        testPathEncodings("this is my input.", "this%20is%20my%20input.")
    }
    "UriEncoder.encodeURLQueryComponent()" {
        UriEncoder.encodeUrlQueryString("path") shouldBe "path"
        UriEncoder.encodeUrlQueryString("/path") shouldBe "/path"
        UriEncoder.encodeUrlQueryString("this is my input.") shouldBe "this%20is%20my%20input."
        UriEncoder.encodeUrlQueryString("one=two") shouldBe "one=two"
        UriEncoder.encodeUrlQueryString("one=two&three=four") shouldBe "one=two&three=four"
        UriEncoder.encodeUrlQueryString("?one=two&three=four") shouldBe "?one=two&three=four"

        UriEncoder.encodeUrlQueryString("path", spaceToPlus = true) shouldBe "path"
        UriEncoder.encodeUrlQueryString("/path", spaceToPlus = true) shouldBe "/path"
        UriEncoder.encodeUrlQueryString("this is my input.", spaceToPlus = true) shouldBe "this+is+my+input."
        UriEncoder.encodeUrlQueryString("one=two", spaceToPlus = true) shouldBe "one=two"
        UriEncoder.encodeUrlQueryString("one=two&three=four", spaceToPlus = true) shouldBe "one=two&three=four"
        UriEncoder.encodeUrlQueryString("?one=two&three=four", spaceToPlus = true) shouldBe "?one=two&three=four"

        UriEncoder.encodeUrlQueryComponent("path") shouldBe "path"
        UriEncoder.encodeUrlQueryComponent("/path") shouldBe "%2Fpath"
        UriEncoder.encodeUrlQueryComponent("this is my input.") shouldBe "this%20is%20my%20input%2E"
        UriEncoder.encodeUrlQueryComponent("one=two") shouldBe "one%3Dtwo"
        UriEncoder.encodeUrlQueryComponent("one=two&three=four") shouldBe "one%3Dtwo%26three%3Dfour"
        UriEncoder.encodeUrlQueryComponent("?one=two&three=four") shouldBe "%3Fone%3Dtwo%26three%3Dfour"

        UriEncoder.encodeUrlQueryComponent("path", spaceToPlus = true) shouldBe "path"
        UriEncoder.encodeUrlQueryComponent("/path", spaceToPlus = true) shouldBe "%2Fpath"
        UriEncoder.encodeUrlQueryComponent("this is my input.", spaceToPlus = true) shouldBe "this+is+my+input%2E"
        UriEncoder.encodeUrlQueryComponent("one=two", spaceToPlus = true) shouldBe "one%3Dtwo"
        UriEncoder.encodeUrlQueryComponent("one=two&three=four", spaceToPlus = true) shouldBe "one%3Dtwo%26three%3Dfour"
        UriEncoder.encodeUrlQueryComponent("?one=two&three=four", spaceToPlus = true) shouldBe "%3Fone%3Dtwo%26three%3Dfour"
    }

    "Uri.parse() with no host info" {
        val uri = UriBuilder.parse("/one/two/and three?a=b&c=d&c=e&key with spaces=value with spaces#/my/doc/fragment")
        uri.protocol shouldBe "http"
//        uri.port shouldBe 80
        uri.host shouldBe "localhost"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe "/my/doc/fragment"
//        uri.toString() shouldBe "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment"
    }

    "Uri.parse()" {
        val uri = UriBuilder.parse("https://www.example.com/one/two/and three?a=b&c=d&c=e&key with spaces=value with spaces#/my/doc/fragment")
        uri.protocol shouldBe "https"
        uri.port shouldBe 443
        uri.host shouldBe "www.example.com"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe "/my/doc/fragment"
        uri.toString() shouldBe "https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment"
    }

    "Uri.parse() with encoded path and query parameters" {
        val uri = UriBuilder.parse("https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment")
        uri.protocol shouldBe "https"
        uri.port shouldBe 443
        uri.host shouldBe "www.example.com"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe "/my/doc/fragment"
        uri.toString() shouldBe "https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment"
    }

    "Uri.parse() with encoded path and query parameters using space as plus" {
        val uri = UriBuilder.parse("https://www.example.com/one/two/and+three?a=b&c=d&c=e&key+with+spaces=value+with+spaces#/my/doc/fragment")
        uri.protocol shouldBe "https"
        uri.port shouldBe 443
        uri.host shouldBe "www.example.com"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and+three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key+with+spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe "/my/doc/fragment"
        uri.toString() shouldBe "https://www.example.com/one/two/and+three?a=b&c=d&c=e&key%2Bwith%2Bspaces=value%20with%20spaces#/my/doc/fragment"
    }

    "Uri.build()" {
        val uri = UriBuilder.build(
            encodedPath = "/one/two/and three",
            encodedQueryString = "a=b&c=d&c=e&key with spaces=value with spaces",
        )
        uri.protocol shouldBe "http"
//        uri.port shouldBe 80 // disabled because the default JS port is 9876 since it's running on a local webserver
        uri.host shouldBe "localhost"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe null
        uri.toString() shouldBe "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces"
    }

    "Uri.build() with encoded path and query parameters using space as plus" {
        val uri = UriBuilder.build(
            encodedPath = "/one/two/and+three",
            encodedQueryString = "a=b&c=d&c=e&key+with+spaces=value+with+spaces",
        )
        uri.protocol shouldBe "http"
        uri.port shouldBe 80
        uri.host shouldBe "localhost"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and+three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key+with+spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe null
        uri.toString() shouldBe "http://localhost/one/two/and+three?a=b&c=d&c=e&key%2Bwith%2Bspaces=value%20with%20spaces"
    }

    "Uri.build() with encoded path and query parameters" {
        val uri = UriBuilder.build(
            encodedPath = "/one/two/and%20three",
            encodedQueryString = "a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces",
        )
        uri.protocol shouldBe "http"
        uri.port shouldBe 80
        uri.host shouldBe "localhost"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe null
        uri.toString() shouldBe "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces"
    }
})
