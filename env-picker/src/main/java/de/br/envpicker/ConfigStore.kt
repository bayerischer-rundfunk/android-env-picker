package de.br.envpicker

import java.lang.ref.WeakReference

/**
 * Singleton holding [WeakReference]s to [Config] objects of active [EnvPicker] instances.
 */
internal object ConfigStore {
    const val KEY = "configKey"

    private val instances = mutableMapOf<String, WeakReference<Config<*>>>()

    /**
     * Store a [Config].
     *
     * @param key The key under which the [Config] will be stored.
     * @param value The [Config] instance to be stored.
     */
    fun set(key: String, value: Config<*>) {
        instances[key] = WeakReference(value)
    }

    /**
     * Retrieve a previously stored [Config].
     *
     * @param key The key under which the [Config] was stored.
     * @return The corresponding [Config] or null if no [Config] was stored under that key or it
     * was garbage-collected as no other references were held.
     */
    fun get(key: String): Config<*>? = instances[key]?.get()
}