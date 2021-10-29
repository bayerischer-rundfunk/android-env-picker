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
    fun `test multi sample`() {
        envPickerSampleMulti(getMockContext())
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

@Suppress("UNUSED_VARIABLE")
fun envPickerSampleMulti(
    context: Context
) {

    // We can use multiple String values with MultiEntry
    val defaultEndpoints =
        listOf(
            MultiEntry("Live", "some.live.endpoint.org", "", ""),
            MultiEntry("Dev", "some.dev.endpoint.org", "some-username", "secretPw")
        )

    // init the library
    val endpointPicker = envPicker(
        "multiEndpointsPicker",
        "Choose Endpoint",
        // Here, we need to define names for the fields, as there are multiple now
        listOf("URL", "User", "Password"),
        defaultEndpoints,
        defaultEndpoints[0],
        context
    )

    // accessing the current endpoint URL now works per index
    val currentlyActiveEndpointUrl = endpointPicker.getActiveEntry(context).fields[0]

}


// a custom data class implementing the Entry interface
data class Endpoint(
    override val name: String, // implement the name field
    val url: String,
    val retryCount: Int,
    val allowHttp: Boolean
) : Entry {
    // return all the custom fields in a constant order here - excluding the name field
    override val fields: List<Any>
        get() = listOf(url, retryCount, allowHttp)

    // the summary of a given entry that is displayed in the UI
    override val summary: String
        get() = "$url, $retryCount retries" + if (allowHttp) ", allowHttp" else ""
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
            EntryDescription(
                // these define each field's type and name that will be displayed in the UI
                listOf(
                    FieldDescription("URL", FieldType.String),
                    FieldDescription("Retry Count", FieldType.Int),
                    FieldDescription("Allow HTTP", FieldType.Boolean),
                ),
                // how to create an Endpoint from user inputs
                { name, fields ->
                    Endpoint(name, fields[0] as String, fields[1] as Int, fields[2] as Boolean)
                },
                // how to serialize it
                // we use Gson here, but you can use any serialization method you like
                { entry -> Gson().toJson(entry) },
                // and how to deserialize it
                { str -> Gson().fromJson(str, Endpoint::class.java) }
            ),
            // which Endpoints should be available per default?
            defaultEndpoints,
            // which endpoint should be active initially?
            defaultEndpoints[0]
        ),
        context
    )

    // accessing the entry's fields now works with the custom field names
    val currentlyActiveEndpointUrl =
        endpointPicker.getActiveEntry(context)?.url
    val currentlyActiveEndpointRetryCount =
        endpointPicker.getActiveEntry(context)?.retryCount
}