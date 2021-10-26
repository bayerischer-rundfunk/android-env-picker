package de.br.android.envpicker.sample

import de.br.android.envpicker.Entry

data class EnvConfig(
    override val name: String,
    val url: String,
    val retryCount: Int,
    val forceHttps: Boolean
) : Entry {
    override val fields: List<Any>
        get() = listOf(url, retryCount, forceHttps)

    override val summary: String
        get() = "$url, $retryCount retries" + if (forceHttps) ", forceHttps" else ""
}