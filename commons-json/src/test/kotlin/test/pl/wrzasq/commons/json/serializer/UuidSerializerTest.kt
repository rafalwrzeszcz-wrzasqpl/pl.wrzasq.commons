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
import pl.wrzasq.commons.json.serializer.UuidSerializer
import java.util.UUID

private const val UUID_STRING = "00000000-0000-0000-0000-000000000000"
private val UUID_PARSED = UUID.fromString(UUID_STRING)

@ExtendWith(MockKExtension::class)
class UuidSerializerTest {
    @MockK
    lateinit var encoder: Encoder

    @MockK
    lateinit var decoder: Decoder

    @Test
    fun deserialize() {
        every { decoder.decodeString() } returns UUID_STRING
        assertEquals(UUID_PARSED, UuidSerializer.deserialize(decoder))
    }

    @Test
    fun serialize() {
        every { encoder.encodeString(any()) } just runs
        UuidSerializer.serialize(encoder, UUID_PARSED)
        verify { encoder.encodeString(UUID_STRING) }
    }
}
