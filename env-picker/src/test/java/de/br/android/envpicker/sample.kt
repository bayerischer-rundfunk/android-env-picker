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
        envPickerSampleSimple(getMockContext(), mockFragmentManager)
    }

    @Test
    fun `test complex sample`() {
        envPickerSample(getMockContext())
    }
}

@Suppress("UNUSED_VARIABLE")
fun envPickerSampleSimple(
    context: Context,
    fragmentManager: FragmentManager
) {

    // the Endpoints that should be available per default
    val defaultEndpoints =
        listOf(
            SimpleEntry("Live", "some.live.endpoint.org"),
            SimpleEntry("Dev", "some.dev.endpoint.org")
        )

    // init the library
    val endpointPicker = envPicker(
        "simpleEndpointsPicker", // used as sharedPrefs key
        "Choose Endpoint", // displayed as fragment title
        defaultEndpoints, // which Endpoints should be available per default?
        defaultEndpoints[0], // which endpoint should be active initially?
        context
    )

    // Initialization is done at this point. Now how to use the EnvPicker?

    // get current endpoint URL
    val currentlyActiveEndpointUrl = endpointPicker.getActiveEntry(context).value

    // change active endpoint
    endpointPicker.setActiveEntry(defaultEndpoints[1], context)

    // update endpoints list
    endpointPicker.setEntries(
        defaultEndpoints + SimpleEntry("Other", "another.url.com"),
        context
    )

    // show management UI
    endpointPicker.startEnvPickerActivity(context)

    // or get an equivalent fragment and handle it yourself
    fragmentManager
        .beginTransaction()
        .add(endpointPicker.createFragment(), "endpointPicker")
        .commit()
}

// a custom data class implementing the Entry interface
data class Endpoint(
    @EntryField("Name")
    override val name: String, // implement the name field
    @EntryField("URL")
    val url: String,
    @EntryField("Retry Count")
    val retryCount: Int,
    @EntryField("Allow HTTP")
    val allowHttp: Boolean
) : Entry {

    // the summary of a given entry that is displayed in the UI
    override val summary: String
        get() = "$url, $retryCount retries" + if (allowHttp) ", allowHttp" else ""

    // optional: define a custom serializer
    class Serializer : EntrySerializer<Endpoint> {
        override fun serializeEntry(entry: Endpoint): String = Gson().toJson(entry)
        override fun deserializeEntry(str: String): Endpoint =
            Gson().fromJson(str, Endpoint::class.java)
    }
}

@Suppress("UNUSED_VARIABLE")
fun envPickerSample(
    context: Context
) {

    // use the custom data class
    val defaultEndpoints =
        listOf(
            Endpoint("Live", "some.live.endpoint.org", 2, false),
            Endpoint("Dev", "some.dev.endpoint.org", 6, true)
        )

    // init the library
    val endpointPicker = envPicker(
        Config(
            "endpointsPicker", // used as sharedPrefs key
            "Choose Endpoint", // displayed as fragment title
            // which Endpoints should be available per default?
            defaultEndpoints,
            // which endpoint should be active initially?
            defaultEndpoints[0],
            // optional: define a custom serializer
            Endpoint.Serializer(),
        ),
        context
    )

    // accessing the entry's fields now works with the custom field names
    val currentlyActiveEndpointUrl =
        endpointPicker.getActiveEntry(context).url
    val currentlyActiveEndpointRetryCount =
        endpointPicker.getActiveEntry(context).retryCount
}