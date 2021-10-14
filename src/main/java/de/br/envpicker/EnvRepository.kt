package de.br.envpicker

import android.content.Context
import java.lang.ref.WeakReference

internal class EnvRepository<T : Entry>(
    private val config: Config<T>,
    private val context: Context
) {
    companion object {
        const val PREFS_KEY_BASE = "de.br.envpicker.prefs_entries."
        const val PREFS_ENTRIES_KEY = "entries"
        const val PREFS_ACTIVE_ENTRY_KEY_KEY = "active_entry"

        const val ENTRY_ORDER_DELIMITER = ":"

        private val instances = mutableMapOf<String, WeakReference<EnvRepository<*>>>()
        fun get(config: Config<*>, context: Context): EnvRepository<*> = synchronized(this) {
            instances[config.key]?.get()
                ?: EnvRepository(config, context).also { instances[config.key] = WeakReference(it) }
        }
    }

    private val prefs
        get() =
            context.getSharedPreferences(PREFS_KEY_BASE + config.key, Context.MODE_PRIVATE)
                ?: throw IllegalStateException()

    fun loadEntries(): List<T> =
        prefs.getStringSet(PREFS_ENTRIES_KEY, null)
            ?.map { it.split(ENTRY_ORDER_DELIMITER, limit = 2) }
            ?.let { pairs -> pairs.sortedBy { it[0] } }
            ?.map { it[1] }
            ?.map(config.entryDescription.deserializeEntry)
            ?: listOf()

    fun loadActiveEntryKey(): String? =
        prefs.getString(PREFS_ACTIVE_ENTRY_KEY_KEY, null)

    fun loadActiveEntry(): T {
        val key = loadActiveEntryKey()
        return loadEntries().find { it.name == key }
            ?: throw IllegalStateException("The active entry key is invalid: $key")
    }

    fun saveActiveEntry(entry: T) {
        prefs.edit().putString(PREFS_ACTIVE_ENTRY_KEY_KEY, entry.name).apply()
    }

    fun saveEntries(state: List<T>) {
        val newSerializedState = state
            .map(config.entryDescription.serializeEntry)
            .mapIndexed { index, s -> "$index$ENTRY_ORDER_DELIMITER$s" }
            .toSet()
        prefs.edit()
            ?.putStringSet(PREFS_ENTRIES_KEY, newSerializedState)
            ?.apply()
    }
}