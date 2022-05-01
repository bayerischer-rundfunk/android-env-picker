package de.br.android.envpicker.ui

import android.content.Context
import androidx.lifecycle.*
import com.jakewharton.processphoenix.ProcessPhoenix
import de.br.android.envpicker.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class EnvViewModel<T : Entry>(
    private val repo: EnvRepository<T>,
    private val config: Config<T>
) : ViewModel() {

    private val entryReflection = EntryReflection(config.defaultActiveEntry)

    private val _items = MutableLiveData<List<EntryContainer<T>>>()
    val items: LiveData<List<EntryContainer<T>>> = _items

    val uiTitle get() = config.uiTitle

    init {
        update()
    }

    fun removeEntry(entry: T) {
        if (entry == repo.loadActiveEntry()) return

        val newState: List<T> = loadState() - entry
        saveState(newState)
        update()
    }

    fun updateEntry(entry: T?, newValues: List<Any>) {
        val newEntry = entryReflection.createEntry(newValues)
        val newState = loadState().toMutableList()
        entry?.let {
            newState.remove(it)
            if (it == repo.loadActiveEntry()) {
                repo.saveActiveEntry(newEntry)
            }
        }
        newState.add(newEntry)
        saveState(newState)
        update()
    }

    fun updateEntryAndRestart(
        entry: T?,
        newValues: List<Any>,
        context: Context
    ) {
        updateEntry(entry, newValues)
        restartApp(context)
    }

    fun setActiveEntryAndRestart(entry: T, context: Context) {
        setActiveEntry(entry)
        restartApp(context)
    }

    fun getFieldDescriptionsAndValues(entry: T?) =
        entryReflection.run {
            if (entry == null) fieldDescriptions.map { it to null }
            else fieldDescriptions.zip(getFieldValues(entry))
        }

    private fun loadState(): List<T> = repo.loadEntries()

    private fun saveState(state: List<T>) = repo.saveEntries(state)

    private fun update() {
        val activeEntryKey = repo.loadActiveEntryKey()
        loadState()
            .map { EntryContainer(it, it.name == activeEntryKey) }
            .let { _items.postValue(it) }
    }

    private fun setActiveEntry(entry: T) {
        repo.saveActiveEntry(entry)
        update()
    }

    private fun restartApp(context: Context) = viewModelScope.launch {
        delay(500)
        ProcessPhoenix.triggerRebirth(context.applicationContext)
    }

    class Factory<T : Entry>(
        private val key: String,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V {
            val config = ConfigStore.get(key) as Config<T>
            return EnvViewModel(EnvRepository(config, context), config) as V
        }
    }
}

internal data class EntryContainer<T : Entry>(
    val entry: T,
    val active: Boolean
)