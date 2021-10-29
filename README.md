# EnvPicker

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.br.android/envpicker/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/de.br.android/envpicker)
![example workflow](https://github.com/bayerischer-rundfunk/android-env-picker/actions/workflows/main.yml/badge.svg)

An Android Library for in-app switching between environment variables.

Central use case is picking a desired endpoint for backend communication, but more complex data
structures can be handled as well. The app will restart after making a change.

![](static/envpicker-overview.jpg)
![](static/envpicker-dialog.jpg)

## Setup

Include EnvPicker in your gradle build like this:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'de.br.android:envpicker:0.1.0'
}
```

## Usage

For simple use cases a key-value pair is a sufficient data structure. Use the `SimpleEntry` class in
such cases.

```kotlin
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
```

In case multiple `String` values need to be saved per entry, the `MultiEntry` class is a good
solution. Usage is similar to `SimpleEntry` above. Alternatively, a custom data class can be chosen
as `Entry` implementation. See the `Advanced Usage` section below.

```kotlin
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

```

## Advanced Usage

In order to support fields of types other than `String` we need to use a custom data class which
implements the `Entry` interface. The UI will display appropriate input methods for each field. See
the supported `FieldType`s below.

```kotlin
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
```

### Supported FieldTypes

The currently supported types for custom data class fields are:

- String
- Int
- Boolean

## Maintainers

- leonbusse
