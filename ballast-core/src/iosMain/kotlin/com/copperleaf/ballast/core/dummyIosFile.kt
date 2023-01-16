package com.copperleaf.ballast.core

/**
 * Library publication fails on iOS because no klib file is generated. Hopefully, adding this dummy file will force the
 * klib to be generated, even though there's nothing useful in it.
 */
private const val dummyIosProperty = 0
