package com.copperleaf.ballast.examples.util

import io.github.copper_leaf.common.BALLAST_VERSION

object ExamplesContext {
    val githubRepo = "https://github.com/copper-leaf/ballast"
    val docsSite = "https://copper-leaf.github.io/ballast"

    val repoBaseUrlWithVersion: String =
        "$githubRepo/tree/$BALLAST_VERSION"
    val sampleSourcesPathInRepo: String =
        "examples/common/src/commonMain/kotlin/com/copperleaf/ballast/examples"
    val samplesUrlWithVersion =
        "${repoBaseUrlWithVersion}/${sampleSourcesPathInRepo}"
}
