package com.copperleaf.ballast.debugger.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A serializer for [LocalDateTime] that uses the ISO-8601 representation.
 *
 * JSON example: `"2007-12-31T23:59:01"`
 *
 * @see LocalDateTime.parse
 * @see LocalDateTime.toString
 *
 * --
 *
 * This class is a copy of the LocalDateTimeIso8601Serializer, but renamed because of IntelliJ Plugin classloader
 * issues. See original source code here and license of that code below.
 *
 * Copyright 2019-2021 JetBrains s.r.o.
 *  * Use of this source code is governed by the Apache 2.0 License that can be found in the LICENSE.txt file.
 *
 *  https://github.com/Kotlin/kotlinx-datetime/blob/94bcc6ff1733c22ef4f937a25a276d3fd728a301/LICENSE.txt
 *  https://github.com/Kotlin/kotlinx-datetime/blob/94bcc6ff1733c22ef4f937a25a276d3fd728a301/LICENSE.txt
 */
public object BallastLocalDateTimeSerializer: KSerializer<LocalDateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }
}
