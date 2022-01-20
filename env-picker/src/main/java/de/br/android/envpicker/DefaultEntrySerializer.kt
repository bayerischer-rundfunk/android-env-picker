package de.br.android.envpicker

import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible

/**
 * Defines methods to serialize an implementation of Entry into a String and vice versa.
 *
 * @property serializeEntry used to serialize the [Entry] implementation
 * @property deserializeEntry used to deserialize the [Entry] implementation
 */
interface EntrySerializer<T : Entry> {
    fun serializeEntry(entry: T): String
    fun deserializeEntry(str: String): T
}

internal class DefaultEntrySerializer<T : Entry>(
    private val entryReflection: EntryReflection<T>
) : EntrySerializer<T> {

    private val separator = "|"

    override fun serializeEntry(entry: T): String =
        entryReflection.fields.map { field ->
            field.getter.isAccessible = true
            field.getter.call(entry)!!
        }
            .joinToString(separator = separator) { value -> encode(serializeField(value)) }

    override fun deserializeEntry(str: String): T {
        val fields = entryReflection.fields
            .map { it.returnType.classifier as KClass<*> }
        val values = str.split(separator)

        if (fields.size != values.size)
            throw IllegalStateException(
                "Number of fields does not match. " +
                        "Expected: ${fields.size}, actual: ${values.size}"
            )

        return values.zip(fields)
            .map { (value, cls) -> deserializeField(decode(value), cls) }
            .let { entryReflection.createEntry(it) }
    }

    private fun serializeField(value: Any) = value.toString()

    @Suppress("UNCHECKED_CAST")
    private fun <U : Any> deserializeField(value: String, fieldClass: KClass<U>): U =
        when (fieldClass) {
            String::class -> value
            Int::class -> value.toInt()
            Boolean::class -> value.toBooleanStrict()
            else -> throw  IllegalArgumentException("Can't deserialize value $value of type $fieldClass.")
        } as U

    private fun encode(str: String) = str.replace(separator, "&sep;")
    private fun decode(str: String) = str.replace("&sep;", separator)
}