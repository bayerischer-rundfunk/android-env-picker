package de.br.android.envpicker.sample

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.br.android.envpicker.Config
import de.br.android.envpicker.envPicker

class SampleViewModel(context: Context) : ViewModel() {
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
                defaultEndpoints,
                defaultEndpoint,
                customSerializer = EnvConfig.Serializer(),
                clearOnChangedDataFormat = false
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
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SampleViewModel(context.applicationContext) as T
        }
    }
}