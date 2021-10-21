package de.br.envpicker

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Configuration of an EnvPicker instance.
 *
 * @property key used as SharedPreferences key
 * @property fragmentTitle displayed as title of fragment
 * @property entryDescription providing meta info about the Entry class to be used
 * @property defaultEntries will be set as initial values if no entries are present at init time
 * @property defaultActiveEntry will be set as initial active entry
 */
data class Config<T : Entry>(
    val key: String,
    val fragmentTitle: String,
    val entryDescription: EntryDescription<T>,
    val defaultEntries: List<T> = listOf(),
    val defaultActiveEntry: T
)

/**
 * A class providing structural information and methods for serialization as well as creation of
 * an [Entry] class.
 *
 * @property fieldNames the names of the fields associated with the [Entry]
 * @property createEntryFromInputs used to instantiate [Entry]s from text inputs
 * @property serializeEntry used to serialize the [Entry] implementation
 * @property deserializeEntry used to deserialize the [Entry] implementation
 */
class EntryDescription<T : Entry>(
    val fieldNames: List<String>,
    val createEntryFromInputs: (String, List<String>) -> T,
    val serializeEntry: (T) -> String,
    val deserializeEntry: (String) -> T,
)

/**
 * An entry to be managed and persisted by the EnvPicker library.
 */
interface Entry {
    val name: String
    val summary: String
    val fields: List<String>
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
 * @sample de.br.envpicker.Endpoint
 * @sample de.br.envpicker.envPickerSample
 */
fun <T : Entry> envPicker(config: Config<T>, context: Context): EnvPicker<T> =
    object : EnvPicker<T> {

        init {
            validateConfig(config)
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
            ConfigStore.set(config.key, config)
            context.startActivity(Intent(context, EnvActivity::class.java).apply {
                putExtra(ConfigStore.KEY, config.key)
            })
        }

        private fun validateConfig(config: Config<T>) {
            if (config.key.isBlank())
                throw IllegalArgumentException("The key can not be blank.")
            if (config.defaultActiveEntry !in config.defaultEntries)
                throw IllegalArgumentException(
                    "The defaultActiveEntry must be included in the defaultEntries. Duh."
                )
            config.defaultEntries.forEach {
                if (it.fields.size != config.entryDescription.fieldNames.size) {
                    throw IllegalArgumentException(
                        "A given default entry does not match the provided Entry description: " +
                                "Wrong number of fields."
                    )
                }
            }
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
 * A simple interface if only one value needs to be stored per entry.
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
    listOf("Value"),
    { name, values -> SimpleEntry(name, values[0]) },
    { entry -> "${entry.name}$SEP${entry.value}" },
    { str -> str.split(SEP).let { SimpleEntry(it[0], it[1]) } },
)

/**
 * A simple interface for storing multiple values per entry. Accessing those values needs to be
 * done via index, so using a custom data class implementing [Entry] might be advisable.
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
 * An implementation of [Entry] that can hold an arbitrary number of fields in addition to the
 * name field.
 *
 * @param name The unique name of this [Entry] displayed in the UI and used as key
 * @param fields A list of [String] values matching the order of field names defined at library initialization
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

private fun multiEntryDescription(fieldNames: List<String>) = EntryDescription(
    fieldNames,
    { name, values -> MultiEntry(name, values) },
    { entry -> "${entry.name}$SEP" + entry.fields.joinToString(separator = SEP) { it } },
    { str -> str.split(SEP).let { MultiEntry(it[0], it.slice(1 until it.size)) } },
)

private const val SEP = "<|>"
