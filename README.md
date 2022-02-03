# EnvPicker

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.br.android/envpicker/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/de.br.android/envpicker)
![example workflow](https://github.com/bayerischer-rundfunk/android-env-picker/actions/workflows/main.yml/badge.svg)

The Android library for in-app management of environment variables.

An easy way to switch among predefined environments or create your own on the fly. Manage string
values like endpoints or more complex data structures that define the entire environment (dev / test
/ live). The convenient UI makes it easy to create, edit and switch among setups. A restart is
triggered after making a change to ensure the app initializes in the new environment.

![](static/envpicker-overview.jpg)
![](static/envpicker-dialog.jpg)

## Setup

Include EnvPicker in your gradle build like this:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'de.br.android:envpicker:0.2.0'
}
```

To use the picker activity, you need to declare it in your manifest within the application tag:

```xml

<activity android:name="de.br.android.envpicker.ui.EnvActivity"
    android:theme="@style/Theme.MaterialComponents.DayNight.NoActionBar" />
```

Of course you can customize the activity theme. However, it needs to be a NoActionBar-based theme.

> Make sure to use either Android Studio Bumblebee (which comes with R8 version 3.1.x) or Kotlin
> version 1.5.x. Otherwise a reflection related exception will pop up.

## Usage

In simple cases the `KeyValueEntry` class can be used to save key-value pairs. Perfect for choosing
an appropriate endpoint.

```kotlin
// Define the Endpoints that should be available per default
val defaultEndpoints =
    listOf(
        KeyValueEntry("Live", "some.live.endpoint.org"),
        KeyValueEntry("Dev", "some.dev.endpoint.org")
    )

// Init the library
val endpointPicker = envPicker(
    key = "keyValueEndpointsPicker", // Used as sharedPrefs key
    fragmentTitle = "Choose Endpoint", // Displayed as title of picker UI
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
```

## Advanced Usage

In order to support fields of types other than `String` or if more values are associated with an
entry, we need to use a custom data class which implements the `Entry` interface. The UI will
display appropriate input methods for each field. See the supported field types below.

```kotlin
// A custom data class implementing the Entry interface
data class Endpoint(
    @EntryField("Name") // Each field needs this annotation to declare a label
    override val name: String, // Override the name field
    @EntryField("URL")
    val url: String,
    @EntryField("Retry Count")
    val retryCount: Int,
    @EntryField("Allow HTTP")
    val allowHttp: Boolean
) : Entry {

    // Optional: The summary of a given entry that is displayed in the UI
    override val summary: String
        get() = "$url, $retryCount retries" + if (allowHttp) ", allowHttp" else ""

    // Optional: Define a custom serializer, e.g. using GSON
    class Serializer : EntrySerializer<Endpoint> {
        override fun serializeEntry(entry: Endpoint): String =
            Gson().toJson(entry)

        override fun deserializeEntry(str: String): Endpoint =
            Gson().fromJson(str, Endpoint::class.java)
    }
}

// Use the custom data class
val defaultEndpoints =
    listOf(
        Endpoint("Live", "some.live.endpoint.org", 2, false),
        Endpoint("Dev", "some.dev.endpoint.org", 6, true)
    )

// Init the library
val endpointPicker = envPicker(
    key = "endpointsPicker", // Used as sharedPrefs key
    fragmentTitle = "Choose Endpoint", // Displayed as title of picker UI
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
```

### Multiple instances

You can easily define multiple instances of `EnvPicker`, each managing another aspect of your environment. Just make sure to provide unique `key`s in each call to `envPicker`!

### Supported field types

The currently supported types for custom data class fields are:

- String
- Int
- Boolean

## Obfuscation

In case you enable obfuscation through R8 and you are using a custom data class, the following needs
to be included in your rules:

```
-keep class <customClassQualifiedName> { *; }
```

## Contribute

Have a look at the [contribution guidelines](./CONTRIBUTING.md) to get started! Please also read
the [code of conduct](./CODE_OF_CONDUCT.md).

## Maintainers

- leonbusse

## License

Apache 2.0. See the [LICENSE](./LICENSE.txt) file for details.
