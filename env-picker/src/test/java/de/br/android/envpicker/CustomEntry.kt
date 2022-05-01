package de.br.android.envpicker

data class CustomEntry(
    @EntryField("Name")
    val key: String,
    @EntryField("Int Field")
    val intField: Int,
    @EntryField("String Field")
    val stringField: String,
    @EntryField("Boolean Field")
    val booleanField: Boolean
) : Entry {
    override val name: String
        get() = key

    override val summary: String
        get() = listOf(intField, stringField, booleanField)
            .joinToString(separator = " - ") { it.toString() }
}