package de.br.android.envpicker.sample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import de.br.android.envpicker.*

class MainViewModel(context: Context) : ViewModel() {
    companion object {
        val defaultEndpoints =
            listOf(
                EnvConfig("Google", "google.com", 1, true),
                EnvConfig("Bing", "bing.com", 2, false),
                EnvConfig("Ecosia", "ecosia.org", 3, true),
            )
        val defaultEndpoint = defaultEndpoints[0]
    }

    private val _currentEndpoint = MutableLiveData<String>()
    val currentEndpoint: LiveData<String> get() = _currentEndpoint

    private val envPicker =
        envPicker(
            Config(
                "envPickerSample",
                "Pick an endpoint!",
                EntryDescription(
                    listOf(
                        FieldDescription("URL", FieldType.String),
                        FieldDescription("Retry Count", FieldType.Int),
                        FieldDescription("Force HTTPS", FieldType.Boolean),
                    ),
                    { name, fields ->
                        EnvConfig(name, fields[0] as String, fields[1] as Int, fields[2] as Boolean)
                    },
                    { Gson().toJson(it) },
                    { Gson().fromJson(it, EnvConfig::class.java) },
                ),
                defaultEndpoints,
                defaultEndpoint
            ),
            context
        )

    init {
        _currentEndpoint.postValue(envPicker.getActiveEntry(context).url)
    }

    fun onChangeEndpoint(context: Context) {
        envPicker.startEnvPickerActivity(context)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(context) as T
        }
    }
}