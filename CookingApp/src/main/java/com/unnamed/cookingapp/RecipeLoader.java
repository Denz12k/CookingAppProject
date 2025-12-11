package com.unnamed.cookingapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RecipeLoader {

    private static final String USER_JSON = System.getProperty("user.home") + "/recipes.json";

    public static List<Recipe> loadRecipes() {
        File file = new File(USER_JSON);

        if (!file.exists()) {
            try (InputStream is = RecipeLoader.class.getResourceAsStream("/recipes.json")) {
                if (is != null) {
                    Files.copy(is, file.toPath());
                } else {
                    System.out.println("resources/recipes.json could not found!");
                    return new ArrayList<>();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Recipe>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static String getUserJsonPath() {
        return USER_JSON;
    }
}