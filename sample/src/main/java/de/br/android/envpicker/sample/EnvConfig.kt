package de.br.android.envpicker.sample

import com.google.gson.Gson
import de.br.android.envpicker.Entry
import de.br.android.envpicker.EntryField
import de.br.android.envpicker.EntrySerializer

data class EnvConfig(
    @EntryField(label = "Name")
    override val name: String,
    @EntryField(label = "URL")
    val url: String,
    @EntryField(label = "Retry Count")
    val retryCount: Int,
    @EntryField(label = "Allow HTTP")
    val allowHttp: Boolean,
) : Entry {
    override val summary: String
        get() = "$url, $retryCount retries" + if (allowHttp) ", allowHttp" else ""

    class Serializer : EntrySerializer<EnvConfig> {
        override fun serialize(entry: EnvConfig): String =
            Gson().toJson(entry)

        override fun deserialize(str: String): EnvConfig =
            Gson().fromJson(str, EnvConfig::class.java)
    }
}