package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.adapter.IngredientAdapter;
import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.model.Ingredient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Screen 1: shows every ingredient currently in the pantry, backed by SQLite,
 * using a RecyclerView + custom adapter. Supports tapping an item to edit it,
 * a delete button per row, and a FAB to add a new item.
 */
import androidx.appcompat.app.AlertDialog;
public class PantryListActivity extends AppCompatActivity implements IngredientAdapter.Listener {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private IngredientAdapter adapter;
    private List<Ingredient> ingredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmptyPantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_pantry);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pantry) {
                return true;
            } else if (id == R.id.nav_recipes) {
                startActivity(new Intent(this, SuggestedRecipesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_settings) {
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
        loadPantry();
    }

    private void loadPantry() {
        ingredients = dbHelper.getAllIngredients();
        tvEmpty.setVisibility(ingredients.isEmpty() ? View.VISIBLE : View.GONE);
        adapter = new IngredientAdapter(ingredients, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEdit(Ingredient ingredient) {
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Remove " + ingredient.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteIngredient(ingredient.getId());
                    Toast.makeText(this, ingredient.getName() + " removed", Toast.LENGTH_SHORT).show();
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
