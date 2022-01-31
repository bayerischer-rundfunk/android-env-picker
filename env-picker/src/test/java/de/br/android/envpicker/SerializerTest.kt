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
            .let { defaultSerializer.serialize(it) }
            .let { defaultSerializer.deserialize(it) }

        assertEquals(entry, entryCopy)
    }

    @Test
    fun `custom serializer - GSON`() {

        class CustomGsonSerializer : EntrySerializer<CustomEntry> {
            private val gson = Gson()

            override fun serialize(entry: CustomEntry): String =
                gson.toJson(entry)

            override fun deserialize(str: String): CustomEntry =
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
            .let { serializer.serialize(it) }
            .let { serializer.deserialize(it) }

        assertEquals(entry, entryCopy)
    }
}
