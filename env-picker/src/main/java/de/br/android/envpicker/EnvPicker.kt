package de.br.android.envpicker

import android.content.Context
import androidx.fragment.app.Fragment
import de.br.android.envpicker.ui.EnvFragment

/**
 * An [EnvPicker] stores environment variables and provides a convenient management UI.
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
 * Creates an instance of [EnvPicker] with the provided configuration.
 *
 * @param config A valid [Config] object.
 * @param context To be used to access SharedPreferences and resources.
 *
 * @sample de.br.android.envpicker.Endpoint
 * @sample de.br.android.envpicker.envPickerSample
 */
fun <T : Entry> envPicker(config: Config<T>, context: Context): EnvPicker<T> =
    EnvPickerImpl(config, context)

/**
 * Creates an instance of [EnvPicker].
 *
 * @property key Used as SharedPreferences key. Needs to be unique among [EnvPicker] instances.
 * @property fragmentTitle Displayed as title of fragment.
 * @property defaultEntries Will be set as initial values if no entries are present at init time.
 * @property defaultActiveEntry Will be set as initial active entry.
 * @property customSerializer Will be used to handle serialization of the entries.
 * @property clearOnChangedDataFormat If true, clears the persisted entries in case of a format change.
 * @param context Will be used to access SharedPreferences and resources.
 */
fun <T : Entry> envPicker(
    key: String,
    fragmentTitle: String,
    defaultEntries: List<T> = listOf(),
    defaultActiveEntry: T,
    context: Context,
    customSerializer: EntrySerializer<T>? = null,
    clearOnChangedDataFormat: Boolean = false,
): EnvPicker<T> =
    envPicker(
        Config(
            key,
            fragmentTitle,
            defaultEntries,
            defaultActiveEntry,
            customSerializer,
            clearOnChangedDataFormat,
        ),
        context,
    )

/**
 * Immutable configuration object holding the complete configuration of an [EnvPicker] instance.
 *
 * @property key Used as SharedPreferences key. Needs to be unique among [EnvPicker] instances.
 * @property fragmentTitle Displayed as title of fragment.
 * @property defaultEntries Will be set as initial values if no entries are present at init time.
 * @property defaultActiveEntry Will be set as initial active entry.
 * @property customSerializer Will be used to handle serialization of the entries.
 * @property clearOnChangedDataFormat If true, clears the persisted entries in case of a format change.
 */
data class Config<T : Entry>(
    val key: String,
    val fragmentTitle: String,
    val defaultEntries: List<T> = listOf(),
    val defaultActiveEntry: T,
    val customSerializer: EntrySerializer<T>?,
    val clearOnChangedDataFormat: Boolean,
)

/**
 * An annotation that marks a property as relevant to the EnvPicker library.
 * All parameters of an [Entry]'s primary constructor need to be marked with this annotation.
 *
 * @param label Will appear in the UI as label for the corresponding field.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EntryField(val label: String)

/**
 * An entry holding some arbitrary data that will be managed and persisted by an [EnvPicker].
 */
interface Entry {
    /**
     * The name that will be displayed in the entry selection screen.
     */
    val name: String

    /**
     * The description that will be displayed in the entry selection screen.
     */
    val summary: String get() = EntryReflection.getDefaultSummary(this)
}

/**
 * An implementation of [Entry] holding a key-value pair.
 *
 * @param name The unique name of this [Entry] displayed in the UI and used as key
 * @param value The [String] value of this [Entry]
 */
data class KeyValueEntry(
    @EntryField("Name")
    override var name: String,
    @EntryField("Value")
    var value: String,
) : Entry

/**
 * Defines methods to serialize an implementation of Entry into a String and vice versa.
 *
 * @property serialize A lambda serializing the corresponding [Entry] implementation into a [String].
 * @property deserialize A lambda deserializing a [String] to the corresponding [Entry] implementation.
 */
interface EntrySerializer<T : Entry> {
    fun serialize(entry: T): String
    fun deserialize(str: String): T
}
