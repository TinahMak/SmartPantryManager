package com.example.smartpantry;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.model.Recipe;
import com.example.smartpantry.model.RecipeIngredient;

/**
 * Screen 4: shows the full ingredient list and method for a single recipe,
 * reached via an Intent extra (the recipe's database id) from either the
 * Suggested Recipes screen.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Recipe recipe = dbHelper.getRecipe(recipeId);

        TextView tvTitle = findViewById(R.id.tvRecipeTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredientsList);
        TextView tvSteps = findViewById(R.id.tvSteps);

        if (recipe != null) {
            toolbar.setTitle(recipe.getName());
            tvTitle.setText(recipe.getName());

            StringBuilder sb = new StringBuilder();
            for (RecipeIngredient ri : recipe.getIngredients()) {
                sb.append("\u2022 ").append(formatQty(ri.getQuantity())).append(" ")
                        .append(ri.getUnit()).append(" ").append(ri.getName()).append("\n");
            }
            tvIngredients.setText(sb.toString().trim());
            tvSteps.setText(recipe.getSteps());
        } else {
            tvTitle.setText("Recipe not found");
        }
    }

    private String formatQty(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
