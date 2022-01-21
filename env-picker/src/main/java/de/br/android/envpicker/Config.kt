package de.br.android.envpicker

/**
 * Configuration of an EnvPicker instance.
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
    private val customSerializer: EntrySerializer<T>?,
    private val clearOnChangedDataFormat: Boolean,
) {
    companion object {
        internal fun getDefaultSummary(entry: Entry) = EntryReflection(entry)
            .getFieldValues(entry, exclude = listOf("name"))
            .joinToString { it?.toString() ?: "" }
    }

    private val entryReflection = EntryReflection(defaultActiveEntry)

    private val defaultEntrySerializer by lazy { DefaultEntrySerializer(entryReflection) }

    internal val fieldDescriptions get() = entryReflection.fieldDescriptions

    internal fun createEntry(values: List<*>) = entryReflection.createEntry(values)

    internal fun getFieldValues(entry: T) = entryReflection.getFieldValues(entry)

    internal fun serializeEntry(value: T): String = try {
        (customSerializer ?: defaultEntrySerializer).serializeEntry(value)
    } catch (e: Exception) {
        throw IllegalStateException("Error serializing entry: $value", e)
    }

    internal fun deserializeEntry(value: String): T? = try {
        (customSerializer ?: defaultEntrySerializer).deserializeEntry(value)
    } catch (e: Exception) {
        if (clearOnChangedDataFormat) null
        else throw IllegalStateException(
            "Error deserializing entry: \"$value\". " +
                    "Set clearOnChangedDataFormat to true if you want a more lenient behavior.",
            e
        )
    }

    internal fun validate() {
        if (key.isBlank())
            throw IllegalArgumentException("The key can not be blank.")
        if (defaultActiveEntry !in defaultEntries)
            throw IllegalArgumentException(
                "The defaultActiveEntry must be included in the defaultEntries."
            )
    }
}