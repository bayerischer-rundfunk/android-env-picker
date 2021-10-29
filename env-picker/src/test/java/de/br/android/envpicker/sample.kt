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
import java.io.Serializable

class SampleTest {

    @Before
    fun setup() {
        mockkObject(EnvFragment)
        every { EnvFragment.create<Endpoint>(any()) } returns mockk(relaxed = true)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any(), any<String>()) } returns mockk()
    }

    @Test
    fun `test Sample`() {
        envPickerSample(getMockContext(), mockFragmentManager)
    }
}

data class Endpoint(
    override val name: String,
    val url: String,
    val user: String,
    val password: String
) : Entry, Serializable {
    override val fields: List<String>
        get() = listOf(url, user, password)

    override val summary: String
        get() = url
}

fun envPickerSample(
    context: Context,
    fragmentManager: FragmentManager
) {

    val defaultEndpoints =
        listOf(
            Endpoint("Live", "some.endpoint.org", "", ""),
            Endpoint("Dev", "dev.some.endpoint.org", "devUser", "secret")
        )

    val endpointPicker = envPicker(
        Config(
            "endpointsPickerSample", // used as sharedPrefs key
            "Choose Endpoint", // displayed as fragment title
            EntryDescription(
                listOf(
                    FieldDescription("URL", FieldType.String),
                    FieldDescription("User", FieldType.String),
                    FieldDescription("Password", FieldType.String),
                ), // these will be displayed as title of TextEdits
                // how to create an Endpoint
                { name, fields ->
                    Endpoint(name, fields[0] as String, fields[1] as String, fields[2] as String)
                },
                // how to serialize it using Gson, you can use any serialization method you like
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

    // Initialization is done at this point. Now how to use the EnvPicker?

    // get current endpoint URL
    val currentlyActiveEndpointUrl = endpointPicker.getActiveEntry(context)?.url
    println("active endpoint: $currentlyActiveEndpointUrl")

    // change active endpoint
    endpointPicker.setActiveEntry(defaultEndpoints[1], context)

    // update endpoints list
    endpointPicker.setEntries(
        defaultEndpoints + Endpoint("Other", "another.url.com", "", ""),
        context
    )

    // show management UI
    fragmentManager
        .beginTransaction()
        .add(endpointPicker.createFragment(), "endpointPicker")
        .commit()
}