package de.br.android.envpicker

import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField


internal class EntryReflection<T : Entry>(instance: T) {

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

    init {
        if (orderedNames.size != orderedFields.size || orderedNames.size != orderedLabels.size)
            throw IllegalArgumentException(
                "Invalid Entry implementation: " +
                        "All constructor parameters must be annotated with @EntryField. " +
                        "Please check your proguard config in case obfuscation is enabled."
            )
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
}


