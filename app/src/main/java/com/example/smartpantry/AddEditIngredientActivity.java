package com.example.smartpantry;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantry.db.DatabaseHelper;
import com.example.smartpantry.model.Ingredient;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Screen 2: Add OR Edit an ingredient (same screen serves both, following the
 * assignment's minimum-screens requirement). Performs input validation before
 * writing to SQLite.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_INGREDIENT_ID = "extra_ingredient_id";

    private DatabaseHelper dbHelper;
    private TextInputEditText etName, etQuantity, etUnit, etExpiry;
    private long editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiry = findViewById(R.id.etExpiry);

        editingId = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);
        if (editingId != -1) {
            toolbar.setTitle("Edit Ingredient");
            Ingredient existing = dbHelper.getIngredient(editingId);
            if (existing != null) {
                etName.setText(existing.getName());
                etQuantity.setText(String.valueOf(existing.getQuantity()));
                etUnit.setText(existing.getUnit());
                etExpiry.setText(existing.getExpiryDate());
            }
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> saveIngredient());
    }

    private void saveIngredient() {
        String name = etName.getText() == null ? "" : etName.getText().toString().trim();
        String qtyStr = etQuantity.getText() == null ? "" : etQuantity.getText().toString().trim();
        String unit = etUnit.getText() == null ? "" : etUnit.getText().toString().trim();
        String expiry = etExpiry.getText() == null ? "" : etExpiry.getText().toString().trim();

        // ---- Input validation ----
        if (TextUtils.isEmpty(name)) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(qtyStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }
        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) {
                etQuantity.setError("Quantity must be greater than 0");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid number");
            etQuantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(unit)) {
            unit = "unit"; // sensible default for countable items
        }
        if (!TextUtils.isEmpty(expiry)) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            sdf.setLenient(false);
            try {
                sdf.parse(expiry);
            } catch (java.text.ParseException e) {
                etExpiry.setError("Enter date as yyyy-MM-dd, e.g. 2026-12-31");
                etExpiry.requestFocus();
                return;
            }
        }

        if (editingId == -1) {
            dbHelper.addIngredient(name, quantity, unit, expiry);
            Toast.makeText(this, name + " added to pantry", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateIngredient(editingId, name, quantity, unit, expiry);
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
