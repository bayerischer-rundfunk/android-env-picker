package de.br.android.envpicker

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import de.br.android.envpicker.ui.EnvActivity
import de.br.android.envpicker.ui.EnvFragment


/**
 * An entry to be managed and persisted by the EnvPicker library.
 */
interface Entry {
    /**
     * The name that will be displayed in the entry selection screen.
     */
    val name: String

    /**
     * The description that will be displayed in the entry selection screen.
     */
    val summary: String

    /**
     * The fields that will be managed and persisted in addition to the name..
     */
    val fields: List<Any>
}

/**
 * An [EnvPicker] can be used to manage its data directly or create a [Fragment] that
 * lets users manage the data via UI.
 */
interface EnvPicker<T : Entry> {
    /**
     * Immutable [Config] provided during initialization.
     */
    val config: Config<T>

    /**
     * Get the persisted [Entry]s directly.
     */
    fun getEntries(context: Context): List<T>

    /**
     * Set the persisted [Entry]s directly.
     */
    fun setEntries(state: List<T>, context: Context)

    /**
     * Get the active [Entry] directly.
     */
    fun getActiveEntry(context: Context): T

    /**
     * Set the active [Entry] directly.
     */
    fun setActiveEntry(entry: T, context: Context)

    /**
     * Create a [Fragment] that lets users manage the [Entry]s via UI. [Entry]s can be added,
     * altered and deleted. One [Entry] can be selected as the active entry. On doing so, the App
     * will restart.
     */
    fun createFragment(): Fragment

    /**
     * Start an Activity that lets users manage the [Entry]s via UI. It simply contains an
     * [EnvFragment].
     */
    fun startEnvPickerActivity(context: Context)
}

/**
 * Instantiates an [EnvPicker] that can be used to manage its data directly or create a [Fragment]
 * that lets users manage the data via UI.
 *
 * For simple use cases there are two simplified overloads of the [envPicker] function. Use this
 * version if you want to use a custom data class as [Entry] implementation.
 *
 * @param config a valid [Config] object
 * @param context to be used to access SharedPreferences and resources
 *
 * @sample de.br.android.envpicker.Endpoint
 * @sample de.br.android.envpicker.envPickerSample
 */
fun <T : Entry> envPicker(config: Config<T>, context: Context): EnvPicker<T> =
    object : EnvPicker<T> {

        init {
            config.validate()
            ConfigStore.set(config.key, config)
            setupDefaultEntries(context)
        }

        @Suppress("UNCHECKED_CAST")
        private fun getRepo(context: Context) =
            (EnvRepository.get(config, context) as EnvRepository<T>)

        override val config = config

        override fun createFragment() = EnvFragment.create<T>(config.key)

        override fun getEntries(context: Context): List<T> =
            getRepo(context).loadEntries()

        override fun setEntries(state: List<T>, context: Context) =
            getRepo(context).saveEntries(state)

        override fun getActiveEntry(context: Context): T =
            getRepo(context).loadActiveEntry()

        override fun setActiveEntry(entry: T, context: Context) {
            getRepo(context).saveActiveEntry(entry)
        }

        override fun startEnvPickerActivity(context: Context) {
            context.startActivity(Intent(context, EnvActivity::class.java).apply {
                putExtra(ConfigStore.KEY, config.key)
            })
        }

        private fun setupDefaultEntries(context: Context) {
            if (getEntries(context).isNullOrEmpty()) {
                setEntries(config.defaultEntries, context)
                setActiveEntry(
                    config.defaultActiveEntry, context
                )
            }
        }
    }

/**
 * A simple interface if only one String value needs to be stored per entry.
 *
 * @param key used as SharedPreferences key
 * @param fragmentTitle displayed as title of fragment
 * @param defaultEntries will be set as initial values if no entries are present at init time
 * @param defaultActiveEntry will be set as initial active entry
 * @param context to be used to access SharedPreferences and resources
 */
fun envPicker(
    key: String,
    fragmentTitle: String,
    defaultEntries: List<SimpleEntry>,
    defaultActiveEntry: SimpleEntry,
    context: Context
): EnvPicker<SimpleEntry> =
    envPicker(
        Config(
            key,
            fragmentTitle,
            SimpleEntryDescription,
            defaultEntries,
            defaultActiveEntry
        ),
        context
    )

/**
 * An implementation of [Entry] representing a single key-value pair.
 *
 * @param name The unique name of this [Entry] displayed in the UI and used as key
 * @param value The [String] value of this [Entry]
 */
data class SimpleEntry(
    override var name: String,
    var value: String,
) : Entry {
    override val summary: String
        get() = value

    override val fields: List<String>
        get() = listOf(summary)
}

private val SimpleEntryDescription = EntryDescription(
    listOf(FieldDescription("Value", FieldType.String)),
    { name, values -> SimpleEntry(name, values[0] as String) },
    { entry -> "${entry.name}$SEP${entry.value}" },
    { str -> str.split(SEP).let { SimpleEntry(it[0], it[1]) } },
)

/**
 * A simple interface for storing multiple String values per entry. Accessing those values needs to
 * be done via index, so using a custom data class implementing [Entry] might be advisable.
 *
 * @param key used as SharedPreferences key
 * @param fragmentTitle displayed as title of fragment
 * @param fieldNames the names of the fields associated with the [Entry]
 * @param defaultEntries will be set as initial values if no entries are present at init time
 * @param defaultActiveEntry will be set as initial active entry
 * @param context to be used to access SharedPreferences and resources
 */
fun envPicker(
    key: String,
    fragmentTitle: String,
    fieldNames: List<String>,
    defaultEntries: List<MultiEntry>,
    defaultActiveEntry: MultiEntry,
    context: Context
): EnvPicker<MultiEntry> =
    envPicker(
        Config(
            key,
            fragmentTitle,
            multiEntryDescription(fieldNames),
            defaultEntries,
            defaultActiveEntry
        ),
        context
    )

/**
 * An implementation of [Entry] that can hold an arbitrary number of String fields in addition to
 * the name field.
 *
 * @param name The unique name of this [Entry] displayed in the UI and used as key.
 * @param fields A list of [String] values matching the order of field names defined at library initialization.
 */
data class MultiEntry(
    override var name: String,
    override val fields: List<String>,
) : Entry {

    constructor(name: String, vararg fields: String) : this(name, fields.asList())

    override val summary: String
        get() = fields
            .filter { it.isNotEmpty() }
            .joinToString(separator = ", ") { it }

    operator fun get(i: Int) = fields[i]
}

@Suppress("UNCHECKED_CAST")
private fun multiEntryDescription(fieldNames: List<String>) = EntryDescription(
    fieldNames.map { FieldDescription(it, FieldType.String) },
    { name, values -> MultiEntry(name, values as List<String>) },
    { entry -> "${entry.name}$SEP" + entry.fields.joinToString(separator = SEP) { it } },
    { str -> str.split(SEP).let { MultiEntry(it[0], it.slice(1 until it.size)) } },
)

private const val SEP = "<|>"
