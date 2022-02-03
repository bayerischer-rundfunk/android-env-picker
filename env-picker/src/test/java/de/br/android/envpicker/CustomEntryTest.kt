package de.br.android.envpicker

import android.content.Context
import com.google.gson.Gson
import de.br.android.envpicker.mocks.getEnclosingFunctionName
import de.br.android.envpicker.mocks.getMockContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CustomEntryTest {

    private lateinit var context: Context

    private val defaultEndpoint1 = CustomEntry(
        "entry #1",
        -45,
        "some value",
        false,
    )
    private val defaultEndpoint2 = CustomEntry(
        "entry #2",
        4,
        "some other value",
        false,
    )
    private val endpoint3 = CustomEntry(
        "entry #3",
        9300,
        "some completely different value",
        true,
    )
    private val defaultEntries = listOf(defaultEndpoint1, defaultEndpoint2)
    private val defaultActiveEntry = defaultEntries[1]

    private val validConfig = Config(
        key = this::class.java.name,
        uiTitle = "Test Fragment",
        defaultEntries = defaultEntries,
        defaultActiveEntry = defaultActiveEntry,
        null,
        false,
    )

    private fun setupEnvPicker() = envPicker(
        validConfig,
        context
    )

    @Before
    fun setup() {
        context = getMockContext()
    }

    /* config validation */

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - empty defaultEntries`() {
        envPicker(
            validConfig.copy(defaultEntries = listOf()),
            context
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - defaultActiveEntry not in defaultEntries`() {
        envPicker(
            validConfig.copy(defaultActiveEntry = endpoint3),
            context
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - blank key`() {
        envPicker(
            validConfig.copy(key = " "),
            context
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invalid config - missing constructor parameter annotations`() {
        data class InvalidCustomEntry(
            val key: String,
            val intField: Int,
            @EntryField("String Field")
            val stringField: String,
            val booleanField: Boolean
        ) : Entry {
            override val name: String
                get() = key

            override val summary: String
                get() = listOf(intField, stringField, booleanField)
                    .joinToString(separator = ", ") { it.toString() }
        }

        class InvalidCustomEntrySerializer : EntrySerializer<InvalidCustomEntry> {
            override fun serialize(entry: InvalidCustomEntry): String =
                Gson().toJson(entry)

            override fun deserialize(str: String): InvalidCustomEntry =
                Gson().fromJson(str, InvalidCustomEntry::class.java)
        }

        val defActiveEntry = InvalidCustomEntry("some key", 23, "some string", false)
        val defEntries = listOf(defActiveEntry)
        envPicker(
            key = getTestKey(),
            "Some title",
            defaultEntries = defEntries,
            defaultActiveEntry = defActiveEntry,
            customSerializer = InvalidCustomEntrySerializer(),
            context = context,
        )
    }

    @Test
    fun `validate valid config`() {
        setupEnvPicker()
    }

    /* basic usage */

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
        envPicker.setActiveEntry(
            CustomEntry("not in the db", 2, "someValue", true),
            context
        )
        envPicker.getActiveEntry(context)
    }

    @Test
    fun `default summary`() {
        data class DefaultSummaryEntry(
            @EntryField("Name")
            override val name: String,
            @EntryField("Int Field")
            val intField: Int,
            @EntryField("String Field")
            val stringField: String,
            @EntryField("Boolean Field")
            val booleanField: Boolean
        ) : Entry

        val defActiveEntry = DefaultSummaryEntry(
            "some name",
            23,
            "some string",
            false
        )
        val defEntries = listOf(defActiveEntry)
        val picker = envPicker(
            getTestKey(),
            "Some title",
            defEntries,
            defActiveEntry,
            context
        )

        assertEquals("23, some string, false", picker.getActiveEntry(context).summary)
    }

    @Test
    fun `custom summary`() {
        val defActiveEntry = CustomEntry(
            "custom summary",
            23,
            "some string",
            false
        )
        val defEntries = listOf(defActiveEntry)
        val picker = envPicker(
            getTestKey(),
            "Some title",
            defEntries,
            defActiveEntry,
            context
        )

        assertEquals("23 - some string - false", picker.getActiveEntry(context).summary)
    }

    private fun getTestKey() = "${this::class.simpleName}.${getEnclosingFunctionName()}"
}
