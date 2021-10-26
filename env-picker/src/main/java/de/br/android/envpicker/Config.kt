package de.br.android.envpicker

import kotlin.reflect.KClass

/**
 * Configuration of an EnvPicker instance.
 *
 * @property key used as SharedPreferences key
 * @property fragmentTitle displayed as title of fragment
 * @property entryDescription providing meta info about the Entry class to be used
 * @property defaultEntries will be set as initial values if no entries are present at init time
 * @property defaultActiveEntry will be set as initial active entry
 */
data class Config<T : Entry>(
    val key: String,
    val fragmentTitle: String,
    val entryDescription: EntryDescription<T>,
    val defaultEntries: List<T> = listOf(),
    val defaultActiveEntry: T
) {

    internal fun validate() {
        if (key.isBlank())
            throw IllegalArgumentException("The key can not be blank.")
        if (defaultActiveEntry !in defaultEntries)
            throw IllegalArgumentException(
                "The defaultActiveEntry must be included in the defaultEntries. Duh."
            )
        defaultEntries.forEach {
            if (it.fields.size != entryDescription.fieldDescriptions.size) {
                throw IllegalArgumentException(
                    "A given default entry does not match the provided Entry description: " +
                            "Wrong number of fields."
                )
            }
        }
        defaultActiveEntry.fields.forEachIndexed { i, field ->
            if (field::class != entryDescription.fieldDescriptions[i].type.cls)
                throw IllegalArgumentException(
                    "A provided FieldDescription does not match the implemented field type: " +
                            "$field - ${entryDescription.fieldDescriptions[i]}"
                )
        }
    }
}


/**
 * A class providing structural information and methods for serialization as well as creation of
 * an [Entry] class.
 *
 * @property fieldDescriptions descriptions of the fields associated with the [Entry]
 * @property createEntryFromInputs used to instantiate [Entry]s from text inputs
 * @property serializeEntry used to serialize the [Entry] implementation
 * @property deserializeEntry used to deserialize the [Entry] implementation
 */
data class EntryDescription<T : Entry>(
    val fieldDescriptions: List<FieldDescription>,
    val createEntryFromInputs: (String, List<Any>) -> T,
    val serializeEntry: (T) -> String,
    val deserializeEntry: (String) -> T,
)

/**
 * Provides meta info about an [Entry]'s field.
 *
 * @property name The name of this field as it will be displayed in the UI.
 * @property type A [FieldType] defining the field's type.
 */
data class FieldDescription(
    val name: String,
    val type: FieldType
)

/**
 * An [Entry] field can have any of these types.
 */
enum class FieldType {
    String,
    Int,
    Boolean;

    val cls: KClass<*>
        get() = when (this) {
            String -> kotlin.String::class
            Int -> kotlin.Int::class
            Boolean -> kotlin.Boolean::class
        }
}