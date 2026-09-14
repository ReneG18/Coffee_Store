# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

This is a single-module Android app (Java) built with Gradle. Root project name is `Assignment3`; the app module is `:app` (package `com.example.assignment3`).

```bash
# Build the app (debug + release)
./gradlew build

# Assemble a debug APK only
./gradlew assembleDebug

# Install the debug build on a connected device/emulator
./gradlew installDebug

# Run JVM unit tests (app/src/test)
./gradlew test

# Run a single unit test class
./gradlew testDebugUnitTest --tests "com.example.assignment3.ExampleUnitTest"

# Run instrumented tests (app/src/androidTest) - requires a connected device/emulator
./gradlew connectedAndroidTest

# Run a single instrumented test class
./gradlew connectedAndroidTest --tests "com.example.assignment3.ExampleInstrumentedTest"

# Lint
./gradlew lint
```

`compileSdk`/`targetSdk` = 36, `minSdk` = 28, Java 11 source/target compatibility.

## Architecture

The app is a simple coffee-ordering demo with no persistence layer, no ViewModel/MVVM, and no navigation component — just two Activities and an adapter, all wired together directly:

- **`MainActivity`** owns all mutable state: it hardcodes the three menu items (`CoffeeItem` instances) directly in `onCreate`, holds the shopping cart as an in-memory `ArrayList<CoffeeItem>`, and recomputes/display the running total itself (`updateCartTotal()`). There is no repository, database, or ViewModel — menu data and cart state live only as long as the Activity.
- **`CoffeeAdapter`** is a standard `RecyclerView.Adapter` that binds `CoffeeItem` fields to `coffee_item.xml` rows. It doesn't mutate the cart itself; instead it reports "add to cart" taps upward through a `CartListener` callback interface, which `MainActivity` implements. This callback pattern (adapter → listener interface → Activity) is the only inter-component communication in the app.
- **`CoffeeItem`** is a plain `Serializable` data model (name, description, price, sizes, drawable resource id). The `sizes` field is populated ("S/M/L") but there is currently no UI/logic that lets a user actually pick a size — all items are added at the base price.
- **`ConfirmationActivity`** is a dumb receiver: `MainActivity` computes the cart total and passes it via a single `double` Intent extra (`ConfirmationActivity.EXTRA_TOTAL`); it does not receive the cart contents itself, only the total.

Data flow for the one user journey the app supports: `MainActivity` builds the menu list → `CoffeeAdapter` renders it and forwards add-to-cart taps via `CartListener` → `MainActivity` appends to `cart` and updates the total label → on checkout, `MainActivity` sums `cart` and starts `ConfirmationActivity` with just that sum.
