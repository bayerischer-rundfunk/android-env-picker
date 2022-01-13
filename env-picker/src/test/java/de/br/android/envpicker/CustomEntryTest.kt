package de.br.android.envpicker

import android.content.Context
import com.google.gson.Gson
import de.br.android.envpicker.mocks.getMockContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CustomEntryTest {

    private data class CustomEntry(
        val key: String,
        val intField: Int,
        val stringField: String,
        val booleanField: Boolean,
        val charField: Char
    ) : Entry {
        override val name: String
            get() = key

        override val fields: List<Any>
            get() = listOf(
                intField,
                stringField,
                booleanField,
                charField.toString()
            )

        override val summary: String
            get() = fields
                .slice(1 until fields.size)
                .joinToString(separator = ", ") { it.toString() }
    }

    private lateinit var context: Context

    private val defaultEndpoint1 = CustomEntry(
        "entry #1",
        -45,
        "some value",
        false,
        'd'
    )
    private val defaultEndpoint2 = CustomEntry(
        "entry #2",
        4,
        "some other value",
        false,
        't'
    )
    private val endpoint3 = CustomEntry(
        "entry #3",
        9300,
        "some completely different value",
        true,
        'z'
    )
    private val defaultEntries = listOf(defaultEndpoint1, defaultEndpoint2)
    private val defaultActiveEntry = defaultEntries[1]

    private val validConfig = Config(
        this::class.java.name,
        "Test Fragment",
        EntryDescription(
            listOf(
                FieldDescription("IntField", FieldType.Int),
                FieldDescription("StringField", FieldType.String),
                FieldDescription("BooleanField", FieldType.Boolean),
                FieldDescription("CharField", FieldType.String),
            ),
            { name, fields ->
                CustomEntry(
                    name,
                    fields[0] as Int,
                    fields[1] as String,
                    fields[2] as Boolean,
                    (fields[3] as String).single()
                )
            },
            { entry -> Gson().toJson(entry) },
            { str -> Gson().fromJson(str, CustomEntry::class.java) }
        ),
        defaultEntries,
        defaultActiveEntry
    )

    private fun setupEnvPicker() = envPicker(
        validConfig,
        context
    )

    @Before
    fun setup() {
        context = getMockContext()
    }

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
    fun `invalid config - FieldType does not match field implementation`() {
        val fieldDescriptions = validConfig.entryDescription.fieldDescriptions.toMutableList()
        fieldDescriptions[0] = fieldDescriptions[0].copy(type = FieldType.Boolean)
        envPicker(
            validConfig.copy(entryDescription = validConfig.entryDescription.copy(fieldDescriptions = fieldDescriptions)),
            context
        )
    }

    @Test
    fun `validate valid config`() {
        setupEnvPicker()
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
        envPicker.setActiveEntry(
            CustomEntry("not in the db", 2, "someValue", true, 'r'),
            context
        )
        envPicker.getActiveEntry(context)
    }
}