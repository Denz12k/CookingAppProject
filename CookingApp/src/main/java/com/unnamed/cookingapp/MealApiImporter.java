package com.unnamed.cookingapp;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class MealApiImporter {

    private static final int BATCH_SIZE = 10;

    private static final Set<String> importedIds = new HashSet<>();

    private static List<String> categoriesCache = null;
    private static Random random = new Random();

    public static List<Recipe> fetchNextBatch() {
        List<Recipe> result = new ArrayList<>();
        int idCounter = 4000 + importedIds.size();

        try {
            if (categoriesCache == null) {
                categoriesCache = new ArrayList<>();
                JSONObject catJson = readJsonFromUrl(
                        "https://www.themealdb.com/api/json/v1/1/categories.php"
                );
                JSONArray cats = catJson.getJSONArray("categories");
                for (int i = 0; i < cats.length(); i++) {
                    categoriesCache.add(cats.getJSONObject(i).getString("strCategory"));
                }
            }

            Set<Integer> usedCategoryIndexes = new HashSet<>();

            while (result.size() < BATCH_SIZE && usedCategoryIndexes.size() < categoriesCache.size()) {

                int catIndex;
                do {
                    catIndex = random.nextInt(categoriesCache.size());
                } while (usedCategoryIndexes.contains(catIndex));
                usedCategoryIndexes.add(catIndex);

                String category = categoriesCache.get(catIndex);

                JSONArray meals = readJsonFromUrl(
                        "https://www.themealdb.com/api/json/v1/1/filter.php?c=" + category
                ).getJSONArray("meals");

                int mealCounter = 0;
                while (mealCounter < meals.length() && result.size() < BATCH_SIZE) {
                    JSONObject meal = meals.getJSONObject(mealCounter++);
                    String mealId = meal.getString("idMeal");

                    if (importedIds.contains(mealId)) continue;
                    importedIds.add(mealId);

                    JSONObject detailJson = readJsonFromUrl(
                            "https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + mealId
                    );
                    JSONObject detail = detailJson.getJSONArray("meals").getJSONObject(0);

                    Recipe r = new Recipe();
                    r.setId(idCounter++);
                    r.setTitle(detail.getString("strMeal"));
                    r.setDuration(20 + random.nextInt(30));
                    r.setInstructions(detail.getString("strInstructions"));
                    r.setImageUrl(detail.getString("strMealThumb"));

                    List<String> ingredients = new ArrayList<>();
                    for (int i = 1; i <= 20; i++) {
                        String ing = detail.optString("strIngredient" + i);
                        if (ing != null && !ing.isBlank()) ingredients.add(ing);
                    }
                    r.setIngredients(ingredients);

                    result.add(r);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private static JSONObject readJsonFromUrl(String urlStr) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }
        reader.close();

        return new JSONObject(json.toString());
    }
}