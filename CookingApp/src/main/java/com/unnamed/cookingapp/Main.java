package com.unnamed.cookingapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    // List of recipes
    private ArrayList<Recipe> recipes = new ArrayList<>();
    private List<Recipe> filteredRecipes = new ArrayList<>();
    private GridPane gridPane = new GridPane();
    private Stage stage;

    // Category -> CheckBox list (dynamically created)
    private final Map<String, List<CheckBox>> categoryCheckboxes = new LinkedHashMap<>();

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        filteredRecipes = recipes;

        // Sample local data (will be filled via API later)
        seedSampleRecipes();

        MenuBar menuBar = new MenuBar();
        HBox bottomHBox = bottomBox();

        VBox root = new VBox(menuBar, bottomHBox);
        root.setStyle("-fx-background-color: #f9f9f9;");

        Scene scene = new Scene(root, 1130, 680);

        stage.setTitle("Recipe Finder");
        stage.setScene(scene);
        stage.show();
    }

    // Simple sort box (sort by title)
    public HBox createSortBox() {
        ComboBox<String> sortOptions = new ComboBox<>();
        sortOptions.getItems().addAll(
                "Sort by Title (A-Z)",
                "Sort by Title (Z-A)",
                "Sort by ID (Ascending)",
                "Sort by ID (Descending)"
        );
        sortOptions.setValue("Sort by Title (A-Z)");

        Button sortButton = new Button("Sort");

        sortButton.setOnAction(e -> {
            String selected = sortOptions.getValue();
            List<Recipe> recentRecipes = filteredRecipes;

            switch (selected) {
                case "Sort by Title (A-Z)" ->
                        recentRecipes.sort(Comparator.comparing(Recipe::getTitle, String.CASE_INSENSITIVE_ORDER));
                case "Sort by Title (Z-A)" ->
                        recentRecipes.sort(Comparator.comparing(Recipe::getTitle, String.CASE_INSENSITIVE_ORDER).reversed());
                case "Sort by ID (Ascending)" ->
                        recentRecipes.sort(Comparator.comparingInt(Recipe::getId));
                case "Sort by ID (Descending)" ->
                        recentRecipes.sort(Comparator.comparingInt(Recipe::getId).reversed());
            }
            createRecipeGrid(recentRecipes);
        });

        Label infoLabel = new Label("🔍 Click on a recipe cover to view its details.");
        infoLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #2a2a2a;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 10 10 120;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hbox = new HBox(10, infoLabel, spacer, sortOptions, sortButton);
        hbox.setPadding(new Insets(10));

        return hbox;
    }

    private HBox bottomBox() {
        VBox left = leftVBox();
        VBox right = rightBox();
        HBox hbox = new HBox(25, left, right);
        hbox.setPadding(new Insets(10));

        return hbox;
    }

    private VBox rightBox() {
        HBox sortBox = createSortBox();
        createRecipeGrid();
        sortBox.setStyle("-fx-background-color: #f0f0f0;");

        ScrollPane scrollPane = new ScrollPane(gridPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f0f0;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox box = new VBox(10, sortBox, scrollPane);
        box.setAlignment(Pos.TOP_CENTER);

        return box;
    }

    private VBox leftVBox() {
        VBox searchBox = new VBox(20);
        searchBox.setPadding(new Insets(10));
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPrefWidth(300);

        Label titleLabel = new Label("RECIPE FINDER");
        titleLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 22));
        titleLabel.setPrefWidth(300);

        TextField searchField = new TextField();
        searchField.setPromptText("Search for a recipe...");

        Button searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);

        VBox searchArea = new VBox(10, searchField, searchButton);

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchButton.fire();
            }
        });

        // CREATE CATEGORIES DYNAMICALLY
        VBox categoriesContainer = new VBox(8);
        categoriesContainer.setAlignment(Pos.TOP_LEFT);
        categoriesContainer.setPadding(new Insets(5));

        // Category -> ingredients (sample data)
        Map<String, List<String>> categoryMap = getDefaultIngredientCategories();

        // Accordion for categories
        Accordion accordion = new Accordion();
        for (Map.Entry<String, List<String>> entry : categoryMap.entrySet()) {
            String categoryName = entry.getKey();
            List<String> items = entry.getValue();

            VBox vbox = new VBox(5);
            vbox.setPadding(new Insets(8));
            List<CheckBox> cbs = new ArrayList<>();
            for (String item : items) {
                CheckBox cb = new CheckBox(item);
                cbs.add(cb);
                vbox.getChildren().add(cb);
            }
            categoryCheckboxes.put(categoryName, cbs);

            TitledPane tp = new TitledPane(categoryName, vbox);
            accordion.getPanes().add(tp);
        }

        // Keep first pane expanded
        if (!accordion.getPanes().isEmpty()) accordion.setExpandedPane(accordion.getPanes().get(0));

        searchButton.setOnAction(e -> {

            List<String> selectedIngredients = getSelectedIngredients();

            String searchText = searchField.getText().trim().toLowerCase();

            filteredRecipes = recipes.stream()
                    .filter(recipe -> {
                        // text match: search in title or instructions
                        boolean textMatch;
                        if (searchText.isEmpty()) {
                            textMatch = true;
                        } else {
                            boolean titleMatch = recipe.getTitle() != null && recipe.getTitle().toLowerCase().contains(searchText);
                            boolean instrMatch = recipe.getInstructions() != null && recipe.getInstructions().toLowerCase().contains(searchText);
                            textMatch = titleMatch || instrMatch;
                        }

                        // ingredients match: all selected ingredients must exist in recipe
                        boolean ingredientsMatch = true;
                        if (!selectedIngredients.isEmpty()) {
                            List<String> recipeIngsLower = recipe.getIngredients().stream()
                                    .map(String::toLowerCase).collect(Collectors.toList());
                            for (String sel : selectedIngredients) {
                                if (!recipeIngsLower.contains(sel.toLowerCase())) {
                                    ingredientsMatch = false;
                                    break;
                                }
                            }
                        }

                        return textMatch && ingredientsMatch;
                    })
                    .collect(Collectors.toList());

            if (filteredRecipes.isEmpty()) {
                showAlert("No Results", null,
                        "No recipes found matching your search/filters.");
            }
            createRecipeGrid(filteredRecipes);
        });

        // Add categories under search area
        categoriesContainer.getChildren().addAll(new Label("Ingredients (by category):"), accordion);

        searchBox.getChildren().addAll(titleLabel, searchArea, categoriesContainer);

        return searchBox;
    }

    private List<String> getSelectedIngredients() {
        List<String> selected = new ArrayList<>();
        for (List<CheckBox> cbs : categoryCheckboxes.values()) {
            for (CheckBox cb : cbs) {
                if (cb.isSelected()) selected.add(cb.getText().trim());
            }
        }
        return selected;
    }

    private void createRecipeGrid() {
        createRecipeGrid(recipes);
    }

    private void createRecipeGrid(List<Recipe> recipesToDisplay) {
        gridPane.getChildren().clear();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));

        int col = 0, row = 0;
        for (Recipe recipe : recipesToDisplay) {
            VBox recipeBox = createRecipeBox(recipe);
            recipeBox.setPrefWidth(150);
            gridPane.add(recipeBox, col, row);
            col++;
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createRecipeBox(Recipe recipe) {
        String imageUrl = recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty() ? "image.jpeg" : recipe.getImageUrl();
        ImageView imageView = new ImageView(new Image(imageUrl, true));
        imageView.setFitWidth(120);
        imageView.setFitHeight(170);
        imageView.setPickOnBounds(true);
        imageView.setOnMouseClicked(e -> openRecipeDetail(recipe));

        Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        imageView.setClip(clip);

        Label nameLabel = new Label(recipe.getTitle());
        nameLabel.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        nameLabel.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(10, imageView, nameLabel);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10));
        vBox.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #cccccc;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        vBox.setPrefWidth(150);

        return vBox;
    }

    private void openRecipeDetail(Recipe recipe) {
        Stage detailStage = new Stage();

        String imageUrl = recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty() ? "image.jpeg" : recipe.getImageUrl();
        ImageView imageView = new ImageView(new Image(imageUrl, true));
        imageView.setFitWidth(200);
        imageView.setFitHeight(300);

        Rectangle clip = new Rectangle(imageView.getFitWidth(), imageView.getFitHeight());
        clip.setArcWidth(10);
        clip.setArcHeight(10);
        imageView.setClip(clip);

        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.DARKBLUE);

        Label idLabel = new Label("ID: " + recipe.getId());

        // ingredients list
        VBox ingredientsBox = new VBox(4);
        ingredientsBox.getChildren().add(new Label("Ingredients:"));
        for (String ing : recipe.getIngredients()) {
            ingredientsBox.getChildren().add(new Label(" - " + ing));
        }

        // instructions
        Label instrLabel = new Label("Instructions:");
        TextArea instrArea = new TextArea(recipe.getInstructions() == null ? "" : recipe.getInstructions());
        instrArea.setWrapText(true);
        instrArea.setEditable(false);
        instrArea.setPrefRowCount(8);

        VBox vBox = new VBox(12, imageView, titleLabel, idLabel, ingredientsBox, instrLabel, instrArea);

        // Edit / Delete
        Button editBtn   = new Button("Edit Recipe");
        Button deleteBtn = new Button("Delete Recipe");
        HBox   btnBar    = new HBox(10, editBtn, deleteBtn);
        btnBar.setAlignment(Pos.CENTER);
        vBox.getChildren().add(btnBar);

        deleteBtn.setOnAction(ev -> {
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
            conf.initOwner(detailStage);
            conf.setTitle("Confirm Deletion");
            conf.setHeaderText(null);
            conf.setContentText(
                    "Delete \"" + recipe.getTitle() + "\" ?"
            );

            conf.getButtonTypes().setAll(
                    ButtonType.YES, ButtonType.CANCEL);

            conf.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.YES) {
                    recipes.remove(recipe);
                    filteredRecipes = recipes;
                    createRecipeGrid();
                    detailStage.close();
                }
            });
        });

        editBtn.setOnAction(ev -> {
            Dialog<Recipe> dialog = new Dialog<>();
            dialog.setTitle("Edit Recipe");
            dialog.setHeaderText("Update the fields and press Save");

            ButtonType saveBtnType =
                    new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes()
                    .addAll(saveBtnType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10); grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titleF = new TextField(recipe.getTitle());
            TextField idF = new TextField(String.valueOf(recipe.getId()));
            TextField imgF = new TextField(recipe.getImageUrl() == null ? "" : recipe.getImageUrl());
            TextArea ingF = new TextArea(String.join(", ", recipe.getIngredients()));
            ingF.setPrefRowCount(4);
            TextArea instrF = new TextArea(recipe.getInstructions() == null ? "" : recipe.getInstructions());
            instrF.setPrefRowCount(6);

            int r = 0;
            grid.addRow(r++, new Label("Title:"),       titleF);
            grid.addRow(r++, new Label("ID:"),          idF);
            grid.addRow(r++, new Label("Image URL:"),   imgF);
            grid.addRow(r++, new Label("Ingredients (comma separated):"), ingF);
            grid.addRow(r++, new Label("Instructions:"), instrF);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(btn -> {
                if (btn == saveBtnType) {
                    try {
                        recipe.setTitle(titleF.getText().trim());
                        recipe.setId(Integer.parseInt(idF.getText().trim()));
                        recipe.setImageUrl(imgF.getText().trim());
                        recipe.setIngredients(parseCsv(ingF.getText()));
                        recipe.setInstructions(instrF.getText().trim());
                        return recipe;
                    } catch (NumberFormatException ex) {
                        new Alert(Alert.AlertType.ERROR,
                                "ID must be numeric!").showAndWait();
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updated -> {
                createRecipeGrid();

                titleLabel.setText(updated.getTitle());
                idLabel.setText("ID: " + updated.getId());
                imageView.setImage(new Image(updated.getImageUrl(), true));

                ingredientsBox.getChildren().clear();
                ingredientsBox.getChildren().add(new Label("Ingredients:"));
                for (String ing : updated.getIngredients()) {
                    ingredientsBox.getChildren().add(new Label(" - " + ing));
                }

                instrArea.setText(updated.getInstructions());
            });
        });

        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(20));
        vBox.layout();
        double prefWidth = vBox.prefWidth(-1) + vBox.getPadding().getLeft() + vBox.getPadding().getRight();
        double prefHeight = vBox.prefHeight(-1) + vBox.getPadding().getTop() + vBox.getPadding().getBottom();

        Scene scene = new Scene(vBox, Math.max(500, prefWidth*1.1), Math.max(600, prefHeight*1.1));
        detailStage.setTitle(recipe.getTitle());
        detailStage.setScene(scene);
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static List<String> parseCsv(String text) {
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    // ----------- Helper: Category + sample ingredient list ---------------
    private Map<String, List<String>> getDefaultIngredientCategories() {
        Map<String, List<String>> m = new LinkedHashMap<>();

        m.put("Produce", Arrays.asList(
                "Tomato", "Onion", "Garlic", "Bell Pepper", "Cucumber", "Spinach", "Lettuce", "Carrot", "Potato", "Lemon"
        ));

        m.put("Dairy & Eggs", Arrays.asList(
                "Milk", "Butter", "Cheese", "Yogurt", "Eggs", "Cream", "Sour Cream"
        ));

        m.put("Meat & Seafood", Arrays.asList(
                "Chicken", "Beef", "Pork", "Salmon", "Shrimp", "Turkey", "Bacon"
        ));

        m.put("Pantry Staples", Arrays.asList(
                "Flour", "Sugar", "Salt", "Rice", "Pasta", "Olive Oil", "Vinegar", "Baking Powder", "Soy Sauce", "Canned Tomatoes"
        ));

        m.put("Herbs & Spices", Arrays.asList(
                "Basil", "Oregano", "Parsley", "Cilantro", "Black Pepper", "Chili Flakes", "Cumin", "Turmeric", "Paprika"
        ));

        m.put("Sauces & Condiments", Arrays.asList(
                "Ketchup", "Mayonnaise", "Mustard", "BBQ Sauce", "Hot Sauce", "Tahini", "Honey", "Peanut Butter"
        ));

        return m;
    }

    // ----------- Sample local recipe data (will be replaced by API) -----------
    private void seedSampleRecipes() {
        if (!recipes.isEmpty()) return;

        Recipe r1 = new Recipe();
        r1.setId(1001);
        r1.setTitle("Tomato Cucumber Salad");
        r1.setImageUrl("https://www.example.com/images/salad.jpg");
        r1.setIngredients(Arrays.asList("Tomato", "Cucumber", "Olive Oil", "Salt", "Lemon"));
        r1.setInstructions("Chop tomato and cucumber. Mix with olive oil, salt and lemon. Serve chilled.");
        recipes.add(r1);

        Recipe r2 = new Recipe();
        r2.setId(1002);
        r2.setTitle("Garlic Butter Salmon");
        r2.setImageUrl("https://www.example.com/images/salmon.jpg");
        r2.setIngredients(Arrays.asList("Salmon", "Garlic", "Butter", "Salt", "Lemon"));
        r2.setInstructions("Sear salmon, add garlic butter sauce, bake for 10 minutes. Serve with lemon.");
        recipes.add(r2);

        Recipe r3 = new Recipe();
        r3.setId(1003);
        r3.setTitle("Pancakes");
        r3.setImageUrl("https://www.example.com/images/pancake.jpg");
        r3.setIngredients(Arrays.asList("Flour", "Milk", "Eggs", "Sugar", "Baking Powder", "Butter"));
        r3.setInstructions("Mix dry ingredients, add milk and eggs. Cook on griddle until golden.");
        recipes.add(r3);

        filteredRecipes = new ArrayList<>(recipes);
    }
}