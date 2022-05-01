package de.br.android.envpicker

import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import de.br.android.envpicker.mocks.getMockContext
import de.br.android.envpicker.mocks.mockFragmentManager
import de.br.android.envpicker.ui.EnvFragment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import org.junit.Before
import org.junit.Test

class SampleTest {

    @Before
    fun setup() {
        mockkObject(EnvFragment)
        every { EnvFragment.create<Endpoint>(any()) } returns mockk(relaxed = true)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any(), any<String>()) } returns mockk()
    }

    @Test
    fun `test simple sample`() {
        envPickerSampleKeyValue(getMockContext(), mockFragmentManager)
    }

    @Test
    fun `test complex sample`() {
        envPickerSample(getMockContext())
    }
}

@Suppress("UNUSED_VARIABLE")
fun envPickerSampleKeyValue(
    context: Context,
    fragmentManager: FragmentManager
) {

    // Define the Endpoints that should be available per default
    val defaultEndpoints =
        listOf(
            KeyValueEntry("Live", "some.live.endpoint.org"),
            KeyValueEntry("Dev", "some.dev.endpoint.org"),
        )

    // Init the library
    val endpointPicker = envPicker(
        key = "keyValueEndpointsPicker", // Used as sharedPrefs key
        uiTitle = "Choose Endpoint", // Displayed as title of picker UI
        defaultEntries = defaultEndpoints, // Which Endpoints should be available per default?
        defaultActiveEntry = defaultEndpoints[0], // Which endpoint should be active initially?
        context = context,
    )

    // Initialization is done at this point. Now how to use the EnvPicker?

    // Get current endpoint URL
    val currentlyActiveEndpointUrl = endpointPicker.getActiveEntry(context).value

    // Change active endpoint
    endpointPicker.setActiveEntry(defaultEndpoints[1], context)

    // Update endpoints list
    endpointPicker.setEntries(
        defaultEndpoints + KeyValueEntry("Other", "another.url.com"),
        context
    )

    // Show management UI
    endpointPicker.startEnvPickerActivity(context)

    // Or get an equivalent fragment and handle it yourself
    fragmentManager
        .beginTransaction()
        .add(endpointPicker.createFragment(), "endpointPicker")
        .commit()
}

// A custom data class implementing the Entry interface
data class Endpoint(
    @EntryField("Name") // Each field needs this annotation to declare a label
    override val name: String, // Override the name field
    @EntryField("URL")
    val url: String,
    @EntryField("Retry Count")
    val retryCount: Int,
    @EntryField("Allow HTTP")
    val allowHttp: Boolean,
) : Entry {

    // Optional: The summary of a given entry that is displayed in the UI
    override val summary: String
        get() = "$url, $retryCount retries" + if (allowHttp) ", allowHttp" else ""

    // Optional: Define a custom serializer, e.g. with GSON
    class Serializer : EntrySerializer<Endpoint> {
        override fun serialize(entry: Endpoint): String =
            Gson().toJson(entry)

        override fun deserialize(str: String): Endpoint =
            Gson().fromJson(str, Endpoint::class.java)
    }
}

@Suppress("UNUSED_VARIABLE")
fun envPickerSample(
    context: Context
) {

    // Use the custom data class
    val defaultEndpoints =
        listOf(
            Endpoint("Live", "some.live.endpoint.org", 2, false),
            Endpoint("Dev", "some.dev.endpoint.org", 6, true),
        )

    // Init the library
    val endpointPicker = envPicker(
        key = "endpointsPicker", // Used as sharedPrefs key
        uiTitle = "Choose Endpoint", // Displayed as title of picker UI
        // Which Endpoints should be available per default?
        defaultEntries = defaultEndpoints,
        // Which endpoint should be active initially?
        defaultActiveEntry = defaultEndpoints[0],
        // Optional: Use your custom serializer
        customSerializer = Endpoint.Serializer(),
        context = context,
    )

// Accessing the current entry now returns an instance of your custom data class
    val currentlyActiveEndpointUrl =
        endpointPicker.getActiveEntry(context).url
    val currentlyActiveEndpointRetryCount =
        endpointPicker.getActiveEntry(context).retryCount
}