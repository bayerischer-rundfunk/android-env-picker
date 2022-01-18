package de.br.android.envpicker

import kotlin.reflect.KClassifier

/**
 * An entry to be managed and persisted by the EnvPicker library.
 */
interface Entry {
    /**
     * The name that will be displayed in the entry selection screen.
     */
    val name: String

    /**
     * The description that will be displayed in the entry selection screen.
     */
    val summary: String get() = Config.getDefaultSummary(this)
}

/**
 * An implementation of [Entry] representing a single key-value pair.
 *
 * @param name The unique name of this [Entry] displayed in the UI and used as key
 * @param value The [String] value of this [Entry]
 */
data class KeyValueEntry(
    @EntryField("Name")
    override var name: String,
    @EntryField("Value")
    var value: String,
) : Entry


/**
 * Needs to be applied to all parameters of an [Entry]'s primary constructor.
 * @param label will appear in the UI as label for the corresponding field.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class EntryField(val label: String)

/**
 * Provides meta info about an [Entry]'s field.
 *
 * @property label The name of this field as it will be displayed in the UI.
 * @property type A [FieldType] defining the field's type.
 */
internal data class FieldDescription(
    val label: String,
    val type: FieldType
)

/**
 * An [Entry] field can have any of these types.
 */
internal enum class FieldType {
    String,
    Int,
    Boolean;

    companion object {
        fun of(cls: KClassifier) = when (cls) {
            kotlin.String::class -> String
            kotlin.Int::class -> Int
            kotlin.Boolean::class -> Boolean
            else -> throw IllegalArgumentException("Unsupported field type: $cls")
        }
    }
}