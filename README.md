# android-env-picker

An Android Library for in-app switching between environment variables.

Central use case is picking a desired endpoint for backend communication, but more complex data can
be handled as well. The app will restart after making a change.

## Architecture

All data is persisted via `SharedPreferences`. The method of serialization can be customized.

## Configuration

Check out the project as git submodule

```bash
git submodule add https://github.com/digitalegarage/android-env-picker
```

Then add it to your Android project as module by adding to your `settings.gradle`

```groovy
include ':android-env-picker'
```

Finally, add the dependency to the module that will use the library

```groovy
implementation project(path: ':android-env-picker')
```

## Usage

This example is taken from the ARD Audiothek code that manages the AMS endpoint in debug mode.

```kotlin

// use the predefined SimpleEntry data class, but alias it to make the code more readable
private typealias AMSEndpoint = SimpleEntry

class AMSEndpointManager(
    private val context: Context
) {
    private companion object {
        // which endpoints to select from per default
        object DefaultAMSEndpoints {
            val LIVE = AMSEndpoint("Live", "accounts.ard.de")
            val BETA = AMSEndpoint("Beta", "accounts-beta.ard.de")
            val TEST = AMSEndpoint("Test", "accounts-test.ard.de")
            val DEV = AMSEndpoint("Dev", "accounts-dev.ard.de")

            val all = listOf(LIVE, BETA, TEST, DEV)
        }
    }

    // an instance of EnvPicker, which is the interface to all functions of the library
    private val envPicker: EnvPicker<AMSEndpoint> by lazy {
        if (BuildConfig.INSPECT) initEnvPicker()
        else illegal() // never instantiate in a live app
    }

    // the central information that we care about
    fun getCurrentEndpointUrl(): String =
        if (BuildConfig.INSPECT)
            envPicker.getActiveEntry(context).value
                .let { (_, url) -> URL("https", url, "/").toString() }
        else BuildConfig.AMS_ENDPOINT

    // this is called from the settings screen
    fun getCurrentEndpointName(): String = envPicker.getActiveEntry(context).name

    // this is called from the settings screen
    fun createEndpointsFragment(): Fragment = envPicker.createFragment()

    private fun initEnvPicker() = envPicker(
        "amsEndpoints", // used as sharedPrefs key
        "Choose AMS Endpoint", // displayed as fragment title
        DefaultAMSEndpoints.all, // which endpoints to select from per default
        getDefaultEndpoint(), // which endpoint should be active per default
        context // used to retrieve resources and SharedPreferences
    )

    // set default endpoint depending on build flavor
    private fun getDefaultEndpoint() = when (BuildConfig.FLAVOR) {
        "master" -> illegal()
        "inspect" -> DefaultAMSEndpoints.LIVE
        else -> DefaultAMSEndpoints.BETA
    }

    private fun illegal(): Nothing =
        throw IllegalStateException("AMS env picker should not be used in prod builds.")
}


```

## Advanced Usage

In case multiple String values need to be saved per entry, the `MultiEntry` class is a good solution.
Usage is similar to `SimpleEntry` above. Alternatively, a custom data class can be chosen as `Entry`
implementation. 

```kotlin

// define a custom data class with arbitrarily many fields of any type
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

val defaultEndpoints =
    listOf(
        Endpoint("Live", "some.endpoint.org", "", ""),
        Endpoint("Dev", "dev.some.endpoint.org", "devUser", "secret")
    )

val endpointPicker = envPicker(
    Config(
        "endpoints", // used as sharedPrefs key
        "Choose Endpoint", // displayed as fragment title
        EntryDescription(
            // these will be displayed as TextEdit titles in the UI and need to correspond to the
            // length of the fields property defined in the custom data class
            listOf("URL", "User", "Password"),
            // how to create an Endpoint
            { name, fields -> Endpoint(name, fields[0], fields[1], fields[2]) },
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
```

## Maintainers

Leon Busse
