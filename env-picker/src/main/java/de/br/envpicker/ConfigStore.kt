package de.br.envpicker

import java.lang.ref.WeakReference

object ConfigStore {
    const val KEY = "configKey"

    private val instances = mutableMapOf<String, WeakReference<Config<*>>>()

    fun set(key: String, value: Config<*>) {
        instances[key] = WeakReference(value)
    }

    fun get(key: String): Config<*>? = instances[key]?.get()
}