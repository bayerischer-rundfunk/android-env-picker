package de.br.envpicker.mocks

import android.content.SharedPreferences

class MockSharedPreferences : SharedPreferences {
    private val strings = mutableMapOf<String, String>()
    private val stringSets = mutableMapOf<String, Set<String>>()

    override fun getAll(): MutableMap<String, *> = strings

    override fun getString(key: String?, defValue: String?): String? =
        strings.getOrDefault(key, null)

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String> =
        (stringSets.getOrDefault(key, null) ?: defValues)
            ?.toMutableSet()
            ?: mutableSetOf()

    override fun getInt(key: String?, defValue: Int): Int = throw NotImplementedError()

    override fun getLong(key: String?, defValue: Long): Long = throw NotImplementedError()

    override fun getFloat(key: String?, defValue: Float): Float = throw NotImplementedError()

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = throw NotImplementedError()

    override fun contains(key: String?): Boolean = throw NotImplementedError()

    override fun edit(): SharedPreferences.Editor =
        object : SharedPreferences.Editor {
            override fun putString(key: String?, value: String?): SharedPreferences.Editor {
                key?.let { strings[it] = value!! }
                return this
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?
            ): SharedPreferences.Editor {
                key?.let { stringSets[it] = values!! }
                return this
            }

            override fun putInt(key: String?, value: Int): SharedPreferences.Editor =
                throw NotImplementedError()

            override fun putLong(key: String?, value: Long): SharedPreferences.Editor =
                throw NotImplementedError()

            override fun putFloat(key: String?, value: Float): SharedPreferences.Editor =
                throw NotImplementedError()

            override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor =
                throw NotImplementedError()

            override fun remove(key: String?): SharedPreferences.Editor {
                stringSets.remove(key)
                strings.remove(key)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                stringSets.clear()
                strings.clear()
                return this
            }

            override fun commit(): Boolean = true

            override fun apply() {
            }
        }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    }
}