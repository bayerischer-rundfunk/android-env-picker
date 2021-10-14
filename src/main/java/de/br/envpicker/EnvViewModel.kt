package de.br.envpicker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jakewharton.processphoenix.ProcessPhoenix
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class EnvViewModel<T : Entry>(
    private val repo: EnvRepository<T>,
    val config: Config<T>
) : ViewModel() {

    private val _items = MutableLiveData<List<EntryContainer<T>>>()
    val items: LiveData<List<EntryContainer<T>>> = _items

    init {
        update()
    }

    val fragmentTitle get() = config.fragmentTitle

    private fun loadState(): List<T> = repo.loadEntries()

    private fun saveState(state: List<T>) = repo.saveEntries(state)

    private fun update() {
        val activeEntryKey = repo.loadActiveEntryKey()
        loadState()
            .map { EntryContainer(it, it.name == activeEntryKey) }
            .let { _items.postValue(it) }
    }

    fun removeEntry(entry: T) {
        if (entry == repo.loadActiveEntry()) return

        val newState: List<T> = loadState() - entry
        saveState(newState)
        update()
    }

    fun updateEntry(entry: T?, newName: String, newValues: List<String>) {
        val newEntry = config.entryDescription.createEntryFromInputs(newName, newValues)
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
        newName: String,
        newValues: List<String>,
        context: Context
    ) {
        updateEntry(entry, newName, newValues)
        restartApp(context)
    }

    private fun setActiveEntry(entry: T) {
        repo.saveActiveEntry(entry)
        update()
    }

    fun setActiveEntryAndRestart(entry: T, context: Context) {
        setActiveEntry(entry)
        restartApp(context)
    }

    private fun restartApp(context: Context) = viewModelScope.launch {
        delay(500)
        ProcessPhoenix.triggerRebirth(context.applicationContext)
    }

    class Factory<T : Entry>(
        private val config: Config<T>,
        private val context: Context
    ) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V {
            return EnvViewModel(EnvRepository(config, context), config) as V
        }
    }
}

internal data class EntryContainer<T : Entry>(
    val entry: T,
    val active: Boolean
)