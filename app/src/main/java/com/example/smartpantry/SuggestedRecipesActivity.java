package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.RecipeAdapter;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.util.MatchingUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen 3: runs the strict-matching logic (see MatchingUtil) against the
 * user's current pantry and lists only recipes that can be made right now,
 * plus an optional "Almost There" section (missing exactly 1 ingredient).
 */
public class SuggestedRecipesActivity extends AppCompatActivity implements RecipeAdapter.Listener {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_recipes);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_recipes) return true;
            if (id == R.id.nav_pantry) {
                startActivity(new Intent(this, PantryListActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        runMatching();
    }

    private void runMatching() {
        List<Ingredient> pantry = dbHelper.getAllIngredients();
        List<Recipe> allRecipes = dbHelper.getAllRecipes();

        List<Recipe> strictMatches = new ArrayList<>();
        List<Recipe> almostThere = new ArrayList<>();

        for (Recipe recipe : allRecipes) {
            if (MatchingUtil.pantryCanMakeRecipe(pantry, recipe.getIngredients())) {
                strictMatches.add(recipe);
            } else if (MatchingUtil.countMissingIngredients(pantry, recipe.getIngredients()) == 1) {
                almostThere.add(recipe);
            }
        }

        RecyclerView rv = findViewById(R.id.rvRecipes);
        TextView tvEmpty = findViewById(R.id.tvNoMatches);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Combine strict matches first, then the "Almost There" bonus list, clearly tagged.
        List<Recipe> combined = new ArrayList<>(strictMatches);
        int strictCount = strictMatches.size();
        combined.addAll(almostThere);

        if (strictMatches.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }

        // Items from index `strictCount` onward are "Almost There" (missing exactly 1),
        // and are visually tagged and separated from the true strict matches above them.
        RecipeAdapter adapter = new RecipeAdapter(combined, this, strictCount);
        rv.setAdapter(adapter);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
