package com.unnamed.cookingapp;

import java.util.ArrayList;
import java.util.List;

public class Recipe {

    private int id;                     // Recipe ID
    private String title;               // Recipe title
    private String imageUrl;            // Recipe image URL
    private List<String> ingredients;   // Ingredients list
    private String instructions;        // Step-by-step instructions

    // Constructor
    public Recipe() {
        this.ingredients = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    // Adders
    public void addIngredient(String ingredient) { ingredients.add(ingredient); }
}