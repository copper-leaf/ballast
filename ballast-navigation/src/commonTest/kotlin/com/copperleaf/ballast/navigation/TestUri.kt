package com.copperleaf.ballast.navigation

import com.copperleaf.ballast.navigation.internal.Uri
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestUri : StringSpec({
    "Uri.encodeURLPathPart()" {
        Uri.encodeUrlPathSegment("path") shouldBe "path"
        Uri.encodeUrlPathSegment("/path") shouldBe "%2Fpath"
        Uri.encodeUrlPathSegment("this is my input.") shouldBe "this%20is%20my%20input."
    }
    "Uri.encodeURLQueryComponent()" {
        Uri.encodeUrlQueryString("path") shouldBe "path"
        Uri.encodeUrlQueryString("/path") shouldBe "/path"
        Uri.encodeUrlQueryString("this is my input.") shouldBe "this%20is%20my%20input."
        Uri.encodeUrlQueryString("one=two") shouldBe "one=two"
        Uri.encodeUrlQueryString("one=two&three=four") shouldBe "one=two&three=four"
        Uri.encodeUrlQueryString("?one=two&three=four") shouldBe "?one=two&three=four"

        Uri.encodeUrlQueryString("path", spaceToPlus = true) shouldBe "path"
        Uri.encodeUrlQueryString("/path", spaceToPlus = true) shouldBe "/path"
        Uri.encodeUrlQueryString("this is my input.", spaceToPlus = true) shouldBe "this+is+my+input."
        Uri.encodeUrlQueryString("one=two", spaceToPlus = true) shouldBe "one=two"
        Uri.encodeUrlQueryString("one=two&three=four", spaceToPlus = true) shouldBe "one=two&three=four"
        Uri.encodeUrlQueryString("?one=two&three=four", spaceToPlus = true) shouldBe "?one=two&three=four"

        Uri.encodeUrlQueryComponent("path") shouldBe "path"
        Uri.encodeUrlQueryComponent("/path") shouldBe "%2Fpath"
        Uri.encodeUrlQueryComponent("this is my input.") shouldBe "this%20is%20my%20input%2E"
        Uri.encodeUrlQueryComponent("one=two") shouldBe "one%3Dtwo"
        Uri.encodeUrlQueryComponent("one=two&three=four") shouldBe "one%3Dtwo%26three%3Dfour"
        Uri.encodeUrlQueryComponent("?one=two&three=four") shouldBe "%3Fone%3Dtwo%26three%3Dfour"

        Uri.encodeUrlQueryComponent("path", spaceToPlus = true) shouldBe "path"
        Uri.encodeUrlQueryComponent("/path", spaceToPlus = true) shouldBe "%2Fpath"
        Uri.encodeUrlQueryComponent("this is my input.", spaceToPlus = true) shouldBe "this+is+my+input%2E"
        Uri.encodeUrlQueryComponent("one=two", spaceToPlus = true) shouldBe "one%3Dtwo"
        Uri.encodeUrlQueryComponent("one=two&three=four", spaceToPlus = true) shouldBe "one%3Dtwo%26three%3Dfour"
        Uri.encodeUrlQueryComponent("?one=two&three=four", spaceToPlus = true) shouldBe "%3Fone%3Dtwo%26three%3Dfour"
    }

    "Uri.parse()" {
        val uri = Uri.parse("https://www.example.com/one/two/and three?a=b&c=d&c=e&key with spaces=value with spaces#/my/doc/fragment")
        uri.protocol shouldBe "https"
        uri.port shouldBe 443
        uri.host shouldBe "www.example.com"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe "/my/doc/fragment"
        uri.toString() shouldBe "https://www.example.com/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment"
    }

    "Uri.parse() with no host info" {
        val uri = Uri.parse("/one/two/and three?a=b&c=d&c=e&key with spaces=value with spaces#/my/doc/fragment")
        uri.protocol shouldBe "http"
        uri.port shouldBe 80
        uri.host shouldBe "localhost"
        uri.decodedPathSegments shouldBe listOf("one", "two", "and three")
        uri.decodedQueryParameters shouldBe mapOf("a" to listOf("b"), "c" to listOf("d", "e"), "key with spaces" to listOf("value with spaces"))
        uri.decodedFragment shouldBe "/my/doc/fragment"
        uri.toString() shouldBe "http://localhost/one/two/and%20three?a=b&c=d&c=e&key%20with%20spaces=value%20with%20spaces#/my/doc/fragment"
    }

    "Uri.build()" {
        val uri = Uri.build(
            encodedPath = "/one/two/and three",
            encodedQueryString = "a=b&c=d&c=e&key with spaces=value with spaces",
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
