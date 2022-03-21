/*
 * This file is part of the pl.wrzasq.commons.
 *
 * @license http://mit-license.org/ The MIT license
 * @copyright 2022 © by Rafał Wrzeszcz - Wrzasq.pl.
 */

package test.pl.wrzasq.commons.json.serializer

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import pl.wrzasq.commons.json.serializer.OffsetDateTimeSerializer
import java.time.OffsetDateTime
import java.time.ZoneOffset

private val OFFSETDATETIME = OffsetDateTime.of(2011, 1, 30, 14, 58, 0, 0, ZoneOffset.ofHours(1))
private const val OFFSETDATETIME_STRING = "2011-01-30T14:58:00+01:00"

@ExtendWith(MockKExtension::class)
class OffsetDateTimeSerializerTest {
    @MockK
    lateinit var encoder: Encoder

    @MockK
    lateinit var decoder: Decoder

    @Test
    fun deserialize() {
        every { decoder.decodeString() } returns OFFSETDATETIME_STRING
        assertEquals(OFFSETDATETIME, OffsetDateTimeSerializer.deserialize(decoder))
    }

    @Test
    fun serialize() {
        every { encoder.encodeString(any()) } just runs
        OffsetDateTimeSerializer.serialize(encoder, OFFSETDATETIME)
        verify { encoder.encodeString(OFFSETDATETIME_STRING) }
    }
}
