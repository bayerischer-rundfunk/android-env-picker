package de.br.android.envpicker

import com.google.gson.Gson
import junit.framework.Assert.assertEquals
import org.junit.Test

class SerializerTest {

    @Test
    fun `default serializer`() {
        val entry = CustomEntry(
            "some name",
            23,
            "some string",
            false
        )
        val defaultSerializer = DefaultEntrySerializer(entry)

        val entryCopy = entry
            .let { defaultSerializer.serializeEntry(it) }
            .let { defaultSerializer.deserializeEntry(it) }

        assertEquals(entry, entryCopy)
    }

    @Test
    fun `custom serializer - GSON`() {

        class CustomGsonSerializer : EntrySerializer<CustomEntry> {
            private val gson = Gson()

            override fun serializeEntry(entry: CustomEntry): String =
                gson.toJson(entry)

            override fun deserializeEntry(str: String): CustomEntry =
                gson.fromJson(str, CustomEntry::class.java)
        }

        val serializer = CustomGsonSerializer()

        val entry = CustomEntry(
            "some name",
            23,
            "some string",
            false
        )

        val entryCopy = entry
            .let { serializer.serializeEntry(it) }
            .let { serializer.deserializeEntry(it) }

        assertEquals(entry, entryCopy)
    }
}
