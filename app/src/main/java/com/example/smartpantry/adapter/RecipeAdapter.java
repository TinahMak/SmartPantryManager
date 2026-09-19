package com.example.smartpantry.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.model.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final Listener listener;
    // Items at this index or later are "Almost There" matches and get tagged.
    // Pass -1 (or a value >= recipes.size()) if no items should be tagged.
    private final int almostThereStartIndex;

    public RecipeAdapter(List<Recipe> recipes, Listener listener, int almostThereStartIndex) {
        this.recipes = recipes;
        this.listener = listener;
        this.almostThereStartIndex = almostThereStartIndex;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.name.setText(recipe.getName());
        holder.count.setText(recipe.getIngredients().size() + " ingredients");
        boolean isAlmostThere = almostThereStartIndex >= 0 && position >= almostThereStartIndex;
        holder.tag.setVisibility(isAlmostThere ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, count, tag;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvRecipeName);
            count = itemView.findViewById(R.id.tvIngredientCount);
            tag = itemView.findViewById(R.id.tvAlmostTag);
        }
    }
}
