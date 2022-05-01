package de.br.android.envpicker

import android.content.Context
import android.content.Intent
import de.br.android.envpicker.ui.EnvActivity
import de.br.android.envpicker.ui.EnvFragment

internal class EnvPickerImpl<T : Entry>(
    override val config: Config<T>,
    context: Context
) : EnvPicker<T> {

    init {
        validateConfig()
        EntryReflection(config.defaultActiveEntry).validate()
        ConfigStore.set(config.key, config)
        setupDefaultEntries(context)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getRepo(context: Context) =
        (EnvRepository.get(config, context) as EnvRepository<T>)

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

    private fun validateConfig() {
        if (config.key.isBlank())
            throw IllegalArgumentException("The key can not be blank.")
        if (config.defaultActiveEntry !in config.defaultEntries)
            throw IllegalArgumentException(
                "The defaultActiveEntry must be included in the defaultEntries."
            )
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