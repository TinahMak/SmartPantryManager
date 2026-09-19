package com.example.smartpantry.util;

import com.example.smartpantry.model.Ingredient;
import com.example.smartpantry.model.RecipeIngredient;

import java.util.List;

/**
 * Contains the "strict-matching" business logic described in the assignment brief:
 * a recipe may only be suggested if EVERY required ingredient is present in the
 * pantry in at least the required quantity.
 *
 * To be reasonably robust to real-world messiness, ingredient names are normalised
 * (case, whitespace, simple plural -> singular) before comparison, and quantities
 * are converted to a common base unit (grams, millilitres, or "count") before being
 * compared, so "2 kg" of flour correctly satisfies a recipe that needs "500 g".
 */
public class MatchingUtil {

    /** Base unit families we can convert between. */
    private enum UnitType { WEIGHT, VOLUME, COUNT, UNKNOWN }

    /**
     * Normalises an ingredient name so that trivial differences
     * (case, whitespace, simple plurals) don't break matching.
     * e.g. "Tomatoes" -> "tomato", "  Onions " -> "onion", "eggs" -> "egg"
     */
    public static String normalizeName(String rawName) {
        if (rawName == null) return "";
        String name = rawName.trim().toLowerCase();
        name = name.replaceAll("\\s+", " ");

        // simple, naive singularisation - good enough for common pantry nouns
        if (name.endsWith("ies") && name.length() > 4) {
            name = name.substring(0, name.length() - 3) + "y";      // berries -> berry
        } else if (name.endsWith("oes") && name.length() > 4) {
            name = name.substring(0, name.length() - 2);            // tomatoes -> tomato
        } else if (name.endsWith("ves") && name.length() > 4) {
            name = name.substring(0, name.length() - 3) + "f";      // loaves -> loaf
        } else if (name.endsWith("ss")) {
            // leave words like "grass" untouched
        } else if (name.endsWith("s") && name.length() > 3) {
            name = name.substring(0, name.length() - 1);            // onions -> onion
        }
        return name;
    }

    private static UnitType unitTypeOf(String unitRaw) {
        if (unitRaw == null) return UnitType.COUNT;
        String u = unitRaw.trim().toLowerCase();
        switch (u) {
            case "g": case "gram": case "grams":
            case "kg": case "kilogram": case "kilograms":
                return UnitType.WEIGHT;
            case "ml": case "milliliter": case "milliliters": case "millilitre": case "millilitres":
            case "l": case "liter": case "liters": case "litre": case "litres":
                return UnitType.VOLUME;
            case "": case "unit": case "units": case "pc": case "pcs": case "piece": case "pieces":
            case "clove": case "cloves":
                return UnitType.COUNT;
            default:
                return UnitType.UNKNOWN;
        }
    }

    /** Converts a quantity into a base unit: grams for weight, millilitres for volume, itself for count. */
    private static double toBaseValue(double quantity, String unitRaw) {
        if (unitRaw == null) return quantity;
        String u = unitRaw.trim().toLowerCase();
        switch (u) {
            case "kg": case "kilogram": case "kilograms":
                return quantity * 1000.0;
            case "l": case "liter": case "liters": case "litre": case "litres":
                return quantity * 1000.0;
            default:
                return quantity;
        }
    }

    /**
     * Returns true only if the pantry contains, for every ingredient the recipe
     * requires, a matching item with quantity >= the amount required.
     */
    public static boolean pantryCanMakeRecipe(List<Ingredient> pantry, List<RecipeIngredient> required) {
        if (required == null || required.isEmpty()) return false;

        for (RecipeIngredient req : required) {
            if (!pantryHasEnoughOf(pantry, req)) {
                return false; // strict rule: ONE missing/insufficient ingredient disqualifies the recipe
            }
        }
        return true;
    }

    /** Returns true if the recipe is missing exactly one ingredient (for the optional "Almost There" list). */
    public static int countMissingIngredients(List<Ingredient> pantry, List<RecipeIngredient> required) {
        int missing = 0;
        for (RecipeIngredient req : required) {
            if (!pantryHasEnoughOf(pantry, req)) missing++;
        }
        return missing;
    }

    private static boolean pantryHasEnoughOf(List<Ingredient> pantry, RecipeIngredient req) {
        String reqName = normalizeName(req.getName());
        UnitType reqType = unitTypeOf(req.getUnit());

        for (Ingredient item : pantry) {
            if (!normalizeName(item.getName()).equals(reqName)) continue;

            UnitType itemType = unitTypeOf(item.getUnit());

            // If either side has an unrecognised/mismatched unit type, fall back to
            // a name-only match (we can't safely compare quantities, but the
            // ingredient IS present) rather than crashing or silently failing.
            if (reqType == UnitType.UNKNOWN || itemType == UnitType.UNKNOWN || reqType != itemType) {
                return true;
            }

            double haveBase = toBaseValue(item.getQuantity(), item.getUnit());
            double needBase = toBaseValue(req.getQuantity(), req.getUnit());
            if (haveBase >= needBase) {
                return true;
            }
        }
        return false;
    }
}
