package de.br.android.envpicker

import android.content.Context
import de.br.android.envpicker.mocks.getMockContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MultiEntryTest {
    private lateinit var context: Context

    private val defaultEndpoint1 = MultiEntry("entry #1", "some value", "other8#\$value-nj")
    private val defaultEndpoint2 =
        MultiEntry("entry #2", "some unusual symbols in this one <|$)(!_)~``öäëïü|><", "")
    private val endpoint3 = MultiEntry("entry #3", "", "random.qwerty.com")

    private val defaultEntries = listOf(defaultEndpoint1, defaultEndpoint2)
    private val defaultActiveEntry = defaultEntries[1]

    private fun setupEnvPicker() = envPicker(
        this::class.java.name,
        "Test Fragment",
        listOf("field1", "field2"),
        defaultEntries,
        defaultActiveEntry,
        context
    )

    @Before
    fun setup() {
        context = getMockContext()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - empty defaultEntries`() {
        envPicker(
            "Test Fragment",
            "testPrefsKey",
            listOf("field1", "field2"),
            listOf(),
            defaultActiveEntry,
            context
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - defaultActiveEntry not in defaultEntries`() {
        envPicker(
            "Test Fragment",
            "testPrefsKey",
            listOf("field1", "field2"),
            defaultEntries,
            endpoint3,
            context
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - blank key`() {
        envPicker(
            " ",
            "testPrefsKey",
            listOf("field1", "field2"),
            defaultEntries,
            defaultEndpoint1,
            context
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - wrong number of field names`() {
        envPicker(
            " ",
            "testPrefsKey",
            listOf("field1"),
            defaultEntries,
            defaultEndpoint1,
            context
        )
    }

    @Test
    fun `basic usage`() {
        val envPicker = setupEnvPicker()

        assertEquals(defaultActiveEntry, envPicker.getActiveEntry(context))
        assertEquals(defaultEntries, envPicker.getEntries(context))

        envPicker.setActiveEntry(defaultEntries[0], context)
        assertEquals(defaultEntries[0], envPicker.getActiveEntry(context))

        envPicker.setEntries(listOf(), context)
        assertTrue(envPicker.getEntries(context).isEmpty())

        envPicker.setEntries(listOf(endpoint3), context)
        assertEquals(endpoint3, envPicker.getEntries(context).first())
    }

    @Test(expected = IllegalStateException::class)
    fun `getActiveEntry on empty data`() {
        val envPicker = setupEnvPicker()
        envPicker.setEntries(listOf(), context)

        envPicker.getActiveEntry(context)
    }

    @Test(expected = IllegalStateException::class)
    fun `getActiveEntry on misconfigured data`() {
        val envPicker = setupEnvPicker()
        envPicker.setActiveEntry(MultiEntry("not in the db", "someValue"), context)

        envPicker.getActiveEntry(context)
    }
}