package com.example.smartpantry.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles all local persistence for the app using SQLiteOpenHelper (raw SQLite,
 * chosen so that the exact CRUD/SQL flow can be explained line-by-line - see
 * README for the justification of this choice over Room/Firebase/PostgreSQL).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smartpantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_P_ID = "_id";
    public static final String COL_P_NAME = "name";
    public static final String COL_P_QTY = "quantity";
    public static final String COL_P_UNIT = "unit";
    public static final String COL_P_EXPIRY = "expiry_date";

    // Recipes table
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_R_ID = "_id";
    public static final String COL_R_NAME = "name";
    public static final String COL_R_STEPS = "steps";

    // Recipe ingredients table
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RI_ID = "_id";
    public static final String COL_RI_RECIPE_ID = "recipe_id";
    public static final String COL_RI_NAME = "ingredient_name";
    public static final String COL_RI_QTY = "quantity";
    public static final String COL_RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " (" +
                COL_P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_P_NAME + " TEXT NOT NULL, " +
                COL_P_QTY + " REAL NOT NULL, " +
                COL_P_UNIT + " TEXT, " +
                COL_P_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_R_NAME + " TEXT NOT NULL, " +
                COL_R_STEPS + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " (" +
                COL_RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_RI_NAME + " TEXT NOT NULL, " +
                COL_RI_QTY + " REAL NOT NULL, " +
                COL_RI_UNIT + " TEXT, " +
                "FOREIGN KEY(" + COL_RI_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_R_ID + "))");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ---------------------------------------------------------------
    // PANTRY CRUD
    // ---------------------------------------------------------------

    public long addIngredient(String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, name);
        cv.put(COL_P_QTY, qty);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        return db.insert(TABLE_PANTRY, null, cv);
    }

    public int updateIngredient(long id, String name, double qty, String unit, String expiry) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_P_NAME, name);
        cv.put(COL_P_QTY, qty);
        cv.put(COL_P_UNIT, unit);
        cv.put(COL_P_EXPIRY, expiry);
        return db.update(TABLE_PANTRY, cv, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_P_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, null, null, null, null, COL_P_NAME + " COLLATE NOCASE ASC");
        while (c.moveToNext()) {
            list.add(new Ingredient(
                    c.getLong(c.getColumnIndexOrThrow(COL_P_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_P_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_P_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_P_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COL_P_EXPIRY))
            ));
        }
        c.close();
        return list;
    }

    public Ingredient getIngredient(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_PANTRY, null, COL_P_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Ingredient result = null;
        if (c.moveToFirst()) {
            result = new Ingredient(
                    c.getLong(c.getColumnIndexOrThrow(COL_P_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_P_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_P_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_P_UNIT)),
                    c.getString(c.getColumnIndexOrThrow(COL_P_EXPIRY))
            );
        }
        c.close();
        return result;
    }

    // ---------------------------------------------------------------
    // RECIPE READ (recipes are seeded once, read-only at runtime)
    // ---------------------------------------------------------------

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_R_NAME + " ASC");
        while (c.moveToNext()) {
            Recipe r = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_R_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_STEPS))
            );
            r.setIngredients(getRecipeIngredients(r.getId()));
            recipes.add(r);
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_R_ID + "=?", new String[]{String.valueOf(recipeId)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(
                    c.getLong(c.getColumnIndexOrThrow(COL_R_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_R_STEPS))
            );
            recipe.setIngredients(getRecipeIngredients(recipeId));
        }
        c.close();
        return recipe;
    }

    public List<RecipeIngredient> getRecipeIngredients(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPE_INGREDIENTS, null, COL_RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(
                    c.getLong(c.getColumnIndexOrThrow(COL_RI_ID)),
                    c.getLong(c.getColumnIndexOrThrow(COL_RI_RECIPE_ID)),
                    c.getString(c.getColumnIndexOrThrow(COL_RI_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_RI_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_RI_UNIT))
            ));
        }
        c.close();
        return list;
    }

    // ---------------------------------------------------------------
    // SEED DATA - 18 recipes, pre-loaded on first run
    // ---------------------------------------------------------------

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Tomato Egg Stir-Fry",
                "1. Beat eggs. 2. Fry tomato until soft. 3. Add eggs and scramble together. 4. Season and serve.",
                new Object[][]{{"egg", 3, "unit"}, {"tomato", 2, "unit"}, {"salt", 1, "g"}});

        addRecipe(db, "Garlic Butter Pasta",
                "1. Boil pasta. 2. Melt butter, fry garlic. 3. Toss pasta in garlic butter. 4. Serve.",
                new Object[][]{{"pasta", 200, "g"}, {"butter", 30, "g"}, {"garlic", 2, "unit"}});

        addRecipe(db, "Vegetable Fried Rice",
                "1. Cook rice ahead of time. 2. Fry onion and carrot. 3. Add rice and soy sauce. 4. Stir-fry and serve.",
                new Object[][]{{"rice", 300, "g"}, {"onion", 1, "unit"}, {"carrot", 1, "unit"}, {"soy sauce", 15, "ml"}});

        addRecipe(db, "Cheese Omelette",
                "1. Beat eggs. 2. Pour into pan. 3. Add cheese, fold. 4. Cook until set.",
                new Object[][]{{"egg", 2, "unit"}, {"cheese", 50, "g"}, {"milk", 30, "ml"}});

        addRecipe(db, "Chicken Stir-Fry",
                "1. Slice chicken. 2. Fry with onion and pepper. 3. Add soy sauce. 4. Cook through and serve.",
                new Object[][]{{"chicken", 300, "g"}, {"onion", 1, "unit"}, {"bell pepper", 1, "unit"}, {"soy sauce", 20, "ml"}});

        addRecipe(db, "Simple Tomato Soup",
                "1. Fry onion and garlic. 2. Add tomato and stock. 3. Simmer 15 min. 4. Blend and serve.",
                new Object[][]{{"tomato", 4, "unit"}, {"onion", 1, "unit"}, {"garlic", 1, "unit"}, {"stock", 500, "ml"}});

        addRecipe(db, "Banana Pancakes",
                "1. Mash banana. 2. Mix with flour, egg and milk. 3. Fry spoonfuls until golden.",
                new Object[][]{{"banana", 2, "unit"}, {"flour", 150, "g"}, {"egg", 1, "unit"}, {"milk", 100, "ml"}});

        addRecipe(db, "Potato and Onion Hash",
                "1. Dice potato and onion. 2. Fry in oil until crisp. 3. Season and serve.",
                new Object[][]{{"potato", 3, "unit"}, {"onion", 1, "unit"}, {"oil", 15, "ml"}});

        addRecipe(db, "Carrot and Lentil Soup",
                "1. Fry onion and carrot. 2. Add lentils and stock. 3. Simmer until soft. 4. Blend.",
                new Object[][]{{"carrot", 2, "unit"}, {"onion", 1, "unit"}, {"lentils", 150, "g"}, {"stock", 500, "ml"}});

        addRecipe(db, "Bread and Cheese Toastie",
                "1. Butter bread. 2. Add cheese between slices. 3. Fry both sides until golden.",
                new Object[][]{{"bread", 2, "unit"}, {"cheese", 60, "g"}, {"butter", 10, "g"}});

        addRecipe(db, "Rice and Beans",
                "1. Cook rice. 2. Warm beans with onion and garlic. 3. Combine and serve.",
                new Object[][]{{"rice", 250, "g"}, {"beans", 200, "g"}, {"onion", 1, "unit"}, {"garlic", 1, "unit"}});

        addRecipe(db, "Spinach and Egg Scramble",
                "1. Wilt spinach in pan. 2. Add beaten eggs. 3. Scramble together. 4. Season and serve.",
                new Object[][]{{"spinach", 100, "g"}, {"egg", 2, "unit"}, {"butter", 10, "g"}});

        addRecipe(db, "Classic Pancakes",
                "1. Mix flour, milk and egg into a batter. 2. Rest 5 min. 3. Fry each side until golden.",
                new Object[][]{{"flour", 200, "g"}, {"milk", 250, "ml"}, {"egg", 2, "unit"}});

        addRecipe(db, "Mushroom Garlic Pasta",
                "1. Boil pasta. 2. Fry mushroom and garlic in butter. 3. Toss together and serve.",
                new Object[][]{{"pasta", 200, "g"}, {"mushroom", 150, "g"}, {"garlic", 2, "unit"}, {"butter", 20, "g"}});

        addRecipe(db, "Chicken and Rice Bowl",
                "1. Cook rice. 2. Fry diced chicken with onion. 3. Serve chicken over rice.",
                new Object[][]{{"chicken", 250, "g"}, {"rice", 200, "g"}, {"onion", 1, "unit"}});

        addRecipe(db, "Apple Cinnamon Oatmeal",
                "1. Cook oats in milk. 2. Dice apple and stir in. 3. Sprinkle cinnamon and serve.",
                new Object[][]{{"oats", 80, "g"}, {"milk", 200, "ml"}, {"apple", 1, "unit"}});

        addRecipe(db, "Egg Fried Noodles",
                "1. Cook noodles. 2. Scramble egg in pan. 3. Add noodles, onion and soy sauce, stir-fry.",
                new Object[][]{{"noodles", 200, "g"}, {"egg", 2, "unit"}, {"onion", 1, "unit"}, {"soy sauce", 15, "ml"}});

        addRecipe(db, "Pumpkin Soup",
                "1. Fry onion. 2. Add pumpkin and stock, simmer until soft. 3. Blend until smooth.",
                new Object[][]{{"pumpkin", 400, "g"}, {"onion", 1, "unit"}, {"stock", 500, "ml"}});
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, Object[][] ingredients) {
        ContentValues cv = new ContentValues();
        cv.put(COL_R_NAME, name);
        cv.put(COL_R_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, cv);

        for (Object[] ing : ingredients) {
            ContentValues icv = new ContentValues();
            icv.put(COL_RI_RECIPE_ID, recipeId);
            icv.put(COL_RI_NAME, (String) ing[0]);
            icv.put(COL_RI_QTY, ((Number) ing[1]).doubleValue());
            icv.put(COL_RI_UNIT, (String) ing[2]);
            db.insert(TABLE_RECIPE_INGREDIENTS, null, icv);
        }
    }
}
