# Smart Pantry Manager

An Android application (Java) that helps users reduce food waste by tracking
ingredients they have at home and suggesting recipes they can cook **strictly**
from what's already in their pantry - no shopping trip required.

## Core Feature: Strict-Matching Rule

A recipe is only suggested if **every** ingredient it needs is present in the
pantry in at least the required quantity. Missing even one ingredient excludes
the recipe from the main list. See `util/MatchingUtil.java` for the logic,
including:
- Name normalisation (case, whitespace, simple plural handling e.g. "tomatoes" -> "tomato")
- Unit-aware quantity comparison (e.g. 1 kg of flour satisfies a 500 g requirement)
- An optional "Almost There" list for recipes missing exactly one ingredient

## Database Choice: SQLite (SQLiteOpenHelper)

SQLite was chosen over Firebase/PostgreSQL because:
- The app is fully offline-first - a user tracking their own pantry has no
  inherent need for cloud sync or multi-device access.
- `SQLiteOpenHelper` gives full, transparent control over the schema and SQL
  used for the strict-matching queries, which made it easier to reason about
  and explain the exact CRUD operations.
- No backend infrastructure or API keys are required to run/mark the project.

## Screens

1. **Pantry List** - RecyclerView of all pantry items (SQLite-backed), with edit/delete.
2. **Add/Edit Ingredient** - single form used for both creating and editing an item, with input validation.
3. **Suggested Recipes** - runs the strict-matching algorithm and lists only recipes the user can make right now.
4. **Recipe Detail** - full ingredient list and method for a selected recipe.
5. **Settings** - expiry alert toggle and units preference (SharedPreferences).

Navigation is via a persistent **BottomNavigationView** across the three main screens.

## Setup / Run Instructions

1. Clone this repository.
2. Open the project folder in Android Studio (Hedgehog or later recommended).
3. Let Gradle sync (requires an internet connection the first time).
4. Run on an emulator or physical device with API 24+.
5. On first launch, the app seeds 18 recipes into the local SQLite database automatically.

## Project Structure

```
app/src/main/java/com/example/smartpantry/
  model/        - Ingredient, Recipe, RecipeIngredient
  db/           - DatabaseHelper (SQLite schema, CRUD, seed data)
  util/         - MatchingUtil (strict-matching business logic)
  adapter/      - IngredientAdapter, RecipeAdapter (RecyclerView)
  *Activity.java - the five screens
```

## Known Limitations / Future Improvements

- Unit conversion covers weight (g/kg) and volume (ml/l) only; mismatched
  unit types fall back to a name-only match.
- No cloud backup - data lives only on the device it was entered on.
