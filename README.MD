# AiMotiveSimulation

Proof-of-concept project for displaying the user's current location and persisting current location data into a local
file on the Android device.

# Running the project

The most convenient way to run the project is to use Android Studio, as described below.

## Requirements

### Tools

1. `Android Studio Jellyfish | 2023.3.1` or a more recent version
2. `Android SDK Build-Tools` `34.0.0` installed from SDK Tools
3. A physical Android device with a version of at least `Android 12` (`API level 31`), also
   having `Google Play Services` installed and available on it
4. `Native Development Kit` (`NDK`) version `26.3.11579264` installed from SDK Tools
5. `CMake` version `3.22.1` installed from SDK Tools

### Configuration

1. A valid URL to a map style
   1.1. The following website must be used to generate the style URL: https://cloud.maptiler.com/maps/
   1.2. The style URL contains a valid API key
   1.3. The style URL is inserted in the `AiMotiveSimulation/local.properties` file, inside the project for the
   following `maplibre.map.style` key, like
   `maplibre.map.style=https://api.maptiler.com/maps/basic-v2/style.json?key=KEY`
   1.4. A setup with a valid Style URL, that can be used:
   `maplibre.map.style=https://api.maptiler.com/maps/basic-v2/style.json?key=vrqCw1UzJOl2WGhmXoTQ`
2. `debug` build variant is used
3. On building the project, `Gradle Offline Mode` should be **disabled** (in order to be able to download dependencies
   from maven repositories, if they have not been already downloaded).

# Main technical aspects

1. Gradle
2. Android Gradle Plugin
3. Kotlin
4. NDK
5. CMake
6. Jetpack Compose
7. Location Services for Android
8. MapLibre GL Native for Android
   8.1. https://github.com/mapbox/mapbox-plugins-android/tree/master
   8.2. https://github.com/maplibre/maplibre-native/blob/main/platform/android/CHANGELOG.md
   8.3. https://maplibre.org/maplibre-native/android/api/index.html
9. MapLibre GL Annotation Plugin for Android
   9.1. https://github.com/mapbox/mapbox-plugins-android/tree/master/plugin-annotation
   9.2. https://github.com/maplibre/maplibre-plugins-android/blob/main/plugin-annotation/CHANGELOG.md

# Main features

1. Handling location access permission
2. Handling location settings (GPS) enablement
3. Diplaying a map
4. Displaying and tracking the current location of the device
5. Persisting current location data on device, to a local file with the help of a C++ library
   5.1. Path to the file on the device is `/data/data/aimotive.simulation/locations.txt`
6. Basic error handling

# Known issues

1. For simplicity, only `android.permission.ACCESS_FINE_LOCATION` is requested and treated. So, if user
   chooses `Approximate location`, the location of the device are not handled.
   1.1. More about the behaviour: https://developer.android.com/develop/sensors-and-location/location/permissions
2. When GPS connectivity is unstable and location request fails, then there is no error message shown, or additional
   logic to handle the case