package de.br.android.envpicker

import kotlin.reflect.KClassifier
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


internal class EntryReflection<T : Entry>(instance: T) {

    companion object {
        internal fun getDefaultSummary(entry: Entry) = EntryReflection(entry)
            .getFieldValues(entry, exclude = listOf("name"))
            .joinToString { it?.toString() ?: "" }
    }

    private val entryClass = instance::class

    private val entryConstructor = entryClass.primaryConstructor
        ?.apply { isAccessible = true }
        ?: throw IllegalArgumentException(
            "${entryClass.qualifiedName} does not have a primary constructor. " +
                    "Please check your proguard config in case obfuscation is enabled."
        )

    private val orderedNames = entryConstructor.parameters.map { it.name!! }

    private val orderedFields = run {
        val fieldMap = entryClass.memberProperties
            .filter { it.javaField != null }
            .map { it.name to it }
            .toMap()
        orderedNames.mapNotNull { fieldMap[it] }
    }

    private val orderedLabels = run {
        val labelMap = entryConstructor.parameters
            .map { it.name to it.annotations.filterIsInstance<EntryField>().firstOrNull()?.label }
            .toMap()
        orderedNames.mapNotNull { labelMap[it] }
    }

    val fields get() = orderedFields

    val fieldDescriptions = orderedFields
        .map { it.returnType.classifier!! }
        .zip(orderedLabels)
        .map { (cls, label) -> FieldDescription(label, FieldType.of(cls)) }

    fun getFieldValues(entry: T, exclude: List<String> = listOf()) =
        orderedFields
            .filter { it.name !in exclude }
            .map { field ->
                field.isAccessible = true
                field.getter.call(entry)
            }

    fun createEntry(values: List<*>) =
        entryConstructor.call(*values.toTypedArray())

    fun validate() {
        if (orderedNames.size != orderedFields.size || orderedNames.size != orderedLabels.size)
            throw IllegalArgumentException(
                "Invalid Entry implementation: " +
                        "All constructor parameters must be annotated with @EntryField. " +
                        "Please check your proguard config in case obfuscation is enabled."
            )
    }
}

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