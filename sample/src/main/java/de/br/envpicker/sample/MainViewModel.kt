package de.br.envpicker.sample

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.br.envpicker.SimpleEntry
import de.br.envpicker.envPicker

class MainViewModel(context: Context) : ViewModel() {
    companion object {
        val defaultEndpoints =
            listOf(
                SimpleEntry("Google", "google.com"),
                SimpleEntry("Bing", "bing.com")
            )
        val defaultEndpoint = defaultEndpoints[0]
    }

    private val _currentEndpoint = MutableLiveData<String>()
    val currentEndpoint: LiveData<String> get() = _currentEndpoint

    private val envPicker =
        envPicker(
            "envPickerSample",
            "Pick an endpoint!",
            defaultEndpoints,
            defaultEndpoint,
            context
        )

    init {
        _currentEndpoint.postValue(envPicker.getActiveEntry(context).value)
    }

    fun onChangeEndpoint(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction().replace(
            R.id.root,
            envPicker.createFragment()
        ).commit()
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(context) as T
        }
    }
}