package com.example.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.Ingredient;

import java.util.List;
import android.content.SharedPreferences;
import android.graphics.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.example.smartpantry.SettingsActivity;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final List<Ingredient> items;
    private final Listener listener;

    public IngredientAdapter(List<Ingredient> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient item = items.get(position);
        holder.name.setText(item.getName());
        String qty = trimZero(item.getQuantity()) + " " + (item.getUnit() == null ? "" : item.getUnit());
        holder.qty.setText(qty.trim());

        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            holder.expiry.setVisibility(View.VISIBLE);

            SharedPreferences prefs = holder.itemView.getContext()
                    .getSharedPreferences(SettingsActivity.PREFS_NAME, android.content.Context.MODE_PRIVATE);
            boolean alertsOn = prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true);
            boolean expiringSoon = false;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date expiryDate = sdf.parse(item.getExpiryDate());
                long daysLeft = (expiryDate.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
                expiringSoon = daysLeft <= 7;
            } catch (ParseException e) {
                expiringSoon = false;
            }

            if (alertsOn && expiringSoon) {
                holder.expiry.setTextColor(Color.RED);
                holder.expiry.setText("⚠ Expires: " + item.getExpiryDate());
            } else {
                holder.expiry.setTextColor(Color.parseColor("#666666"));
                holder.expiry.setText("Expires: " + item.getExpiryDate());
            }
        } else {
            holder.expiry.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(item));
    }

    private String trimZero(double d) {
        if (d == Math.floor(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, qty, expiry, deleteBtn;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvIngredientName);
            qty = itemView.findViewById(R.id.tvIngredientQty);
            expiry = itemView.findViewById(R.id.tvExpiry);
            deleteBtn = itemView.findViewById(R.id.btnDelete);
        }
    }
}
