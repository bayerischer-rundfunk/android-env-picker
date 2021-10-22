# android-env-picker

An Android Library for in-app switching between environment variables.

Central use case is picking a desired endpoint for backend communication, but more complex data can
be handled as well. The app will restart after making a change.

## Architecture

All data is persisted via `SharedPreferences`. The method of serialization can be customized.

## Usage

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
endpointPicker.startEnvPickerActivity(context)

// or handle the fragment yourself
fragmentManager
    .beginTransaction()
    .add(endpointPicker.createFragment(), "endpointPicker")
    .commit()
```

## Maintainers

- leonbusse
