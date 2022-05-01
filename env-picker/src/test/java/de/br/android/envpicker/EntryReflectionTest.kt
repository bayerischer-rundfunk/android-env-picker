package de.br.android.envpicker

import junit.framework.Assert.assertEquals
import org.junit.Test

class EntryReflectionTest {

    @Test
    fun `create entry`() {
        val entry = CustomEntry(
            "some name",
            23,
            "some string",
            false
        )
        val entryReflection = EntryReflection(entry)
        val created = entryReflection.createEntry(listOf("entry #3", 23, "fkjw", true))
        val expected = CustomEntry("entry #3", 23, "fkjw", true)
        assertEquals(expected, created)
    }

    private data class PrivateEntry(
        @EntryField("Name")
        override val name: String,
        @EntryField("Int Field")
        val intField: Int,
        @EntryField("String Field")
        val stringField: String,
        @EntryField("Boolean Field")
        val booleanField: Boolean
    ) : Entry

    @Test
    fun `private constructor`() {
        val entry = PrivateEntry("some key", 23, "some string", false)
        val entryReflection = EntryReflection(entry)

        assertEquals(
            entry,
            entryReflection.createEntry(listOf("some key", 23, "some string", false))
        )
    }
}