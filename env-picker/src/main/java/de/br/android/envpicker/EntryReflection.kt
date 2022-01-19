package de.br.android.envpicker

import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


internal class EntryReflection<T : Entry>(instance: T) {

    private val entryClass = instance::class

    internal val entryConstructor = entryClass.primaryConstructor
        ?.apply { isAccessible = true }
        ?: throw IllegalArgumentException("${entryClass.qualifiedName} does not have a primary constructor.")

    internal val entryLabelsAndFields = run {
        val orderedNames = entryConstructor.parameters
            .map { it.name }
        val labelsMap = entryConstructor.parameters
            .map { it.name to it.annotations.filterIsInstance<EntryField>().firstOrNull()?.label }
            .toMap()
        val propertiesMap = entryClass.memberProperties
            .filter { it.javaField != null }
            .map { it.name to it }
            .toMap()
        val orderedProperties = orderedNames.map { propertiesMap[it]!! }
        val orderedLabels = orderedNames.map { labelsMap[it]!! }
        orderedLabels.zip(orderedProperties)
    }

    internal val fieldDescriptions =
        entryLabelsAndFields
            .map { (label, field) -> label to field.returnType.classifier!! }
            .map { (label, cls) -> FieldDescription(label, FieldType.of(cls)) }

    init {
        if (entryLabelsAndFields.size != entryConstructor.parameters.size) {
            throw IllegalArgumentException(
                "Invalid Entry implementation: " +
                        "All constructor parameters must be annotated with @${EntryField::class.simpleName}. " +
                        "No other fields are allowed."
            )
        }
    }

    internal fun getFieldValues(entry: T, exclude: List<String> = listOf()) =
        entryLabelsAndFields
            .filter { (_, field) -> field.name !in exclude }
            .map { (_, field) ->
                field.isAccessible = true
                field.getter.call(entry)
            }

    internal fun createEntry(values: List<*>) =
        entryConstructor.call(*values.toTypedArray())
}


