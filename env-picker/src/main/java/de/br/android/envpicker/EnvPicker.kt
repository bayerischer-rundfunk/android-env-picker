package de.br.android.envpicker

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import de.br.android.envpicker.ui.EnvActivity
import de.br.android.envpicker.ui.EnvFragment


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
 * For simple key-value cases there is a simplified overload of the [envPicker] function.
 * Use this version if you want to use a custom data class as [Entry] implementation.
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
    envPicker(Config(key, fragmentTitle, defaultEntries, defaultActiveEntry), context)

/**
 * An implementation of [Entry] representing a single key-value pair.
 *
 * @param name The unique name of this [Entry] displayed in the UI and used as key
 * @param value The [String] value of this [Entry]
 */
data class SimpleEntry(
    @EntryField("Name")
    override var name: String,
    @EntryField("Value")
    var value: String,
) : Entry
