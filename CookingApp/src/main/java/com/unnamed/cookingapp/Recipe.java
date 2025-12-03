package com.unnamed.cookingapp;

import java.util.ArrayList;
import java.util.List;

public class Recipe {

    private int id;                     // Recipe ID
    private String title;               // Recipe title
    private String imageUrl;            // Recipe image URL
    private List<String> ingredients;   // Ingredients list
    private String instructions;        // Step-by-step instructions
    private int duration;               // Recipe duration in minutes

    // Default image URL
    private static final String DEFAULT_IMAGE_URL =
            "https://i0.wp.com/pediaa.com/wp-content/uploads/2021/09/Food.jpg?resize=570%2C380&ssl=1";

    // Constructor
    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.imageUrl = DEFAULT_IMAGE_URL;
        this.duration = 0; // Default duration 0
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            this.imageUrl = DEFAULT_IMAGE_URL;
        } else {
            this.imageUrl = imageUrl;
        }
    }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    // Adders
    public void addIngredient(String ingredient) { ingredients.add(ingredient); }
}