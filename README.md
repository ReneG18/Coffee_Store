# Coffee Store

A simple Android coffee-ordering app built in Java. Users browse a small menu, add items to a cart, and check out to see the order total.

## Features

- Menu screen (`MainActivity`) listing coffee items in a `RecyclerView`, each with an image, name, description, price, and an "Add to Cart" button
- Running cart total displayed on the menu screen as items are added
- Checkout flow that passes the cart total to a confirmation screen (`ConfirmationActivity`)

## Tech stack

- Java, Android SDK (`minSdk` 28, `targetSdk`/`compileSdk` 36)
- AndroidX: AppCompat, Material Components, ConstraintLayout, Activity
- Gradle (Kotlin DSL) with a version catalog (`gradle/libs.versions.toml`)
- JUnit + AndroidX Test/Espresso for unit and instrumented tests

## Project structure

```
app/src/main/java/com/example/assignment3/
  MainActivity.java          # Menu screen, cart state, checkout
  CoffeeAdapter.java         # RecyclerView adapter for the menu list
  CoffeeItem.java            # Coffee menu item model
  ConfirmationActivity.java  # Order total confirmation screen
app/src/main/res/            # Layouts, drawables, and other resources
```

## Getting started

Open the project in Android Studio, or build/test from the command line:

```bash
# Build the app
./gradlew build

# Install the debug build on a connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires a connected device/emulator)
./gradlew connectedAndroidTest
```

See [CLAUDE.md](CLAUDE.md) for more detailed build/test commands and an architecture overview.
