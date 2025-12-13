package com.unnamed.cookingapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
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

    private ArrayList<Recipe> recipes = new ArrayList<>();
    private List<Recipe> filteredRecipes = new ArrayList<>();
    private GridPane gridPane = new GridPane();
    private Stage stage;

    private final Map<String, List<CheckBox>> categoryCheckboxes = new LinkedHashMap<>();

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        filteredRecipes = recipes;

        seedSampleRecipes();

        MenuBar menuBar = createMenuBar();
        HBox bottomHBox = bottomBox();

        VBox root = new VBox(menuBar, bottomHBox);
        root.setStyle("-fx-background-color: #f9f9f9;");

        Scene scene = new Scene(root, 1130, 680);

        stage.setTitle("Recipe Finder App");
        Image icon = new Image(getClass().getResource("/images/image.png").toExternalForm());
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // ------------------ Recipe Menu ------------------
        Menu recipeMenu = new Menu("Recipe");

        MenuItem addRecipe = new MenuItem("Add New Recipe");
        addRecipe.setOnAction(e -> openAddRecipeDialog());

        /* MenuItem importFromApi = new MenuItem("Import 10 Recipes from API");
        importFromApi.setOnAction(e -> {
            List<Recipe> newRecipes = MealApiImporter.fetchNextBatch();

            if (newRecipes.isEmpty()) {
                showAlert("Info", null, "No more recipes to import.");
                return;
            }

            recipes.addAll(newRecipes);
            filteredRecipes = new ArrayList<>(recipes);
            RecipeSaver.saveRecipes(recipes);
            createRecipeGrid(filteredRecipes);

            showAlert("Success", null,
                    newRecipes.size() + " new recipes imported!");
        }); */

        recipeMenu.getItems().addAll(addRecipe /* , importFromApi */ );

        // ------------------ Help Menu ------------------
        Menu helpMenu = new Menu("Help");

        MenuItem helpItem = new MenuItem("User Manual");
        helpItem.setOnAction(e -> {
            Stage manualStage = new Stage();
            manualStage.setTitle("📘 Recipe Finder - User Manual");

            VBox contentBox = new VBox(20);
            contentBox.setPadding(new Insets(20));
            contentBox.setStyle("-fx-background-color: #f9f9f9;");
            contentBox.setAlignment(Pos.TOP_LEFT);

            Label title = new Label("📘 Recipe Finder - User Manual");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
            title.setTextFill(Color.DARKBLUE);

            TitledPane overview = createSection("🗂 1. Overview",
                    "This app helps you manage recipes.\n" +
                            "You can add, edit, delete, search, filter, and sort recipes.");

            TitledPane addRecipe_ = createSection("➕ 2. Adding a Recipe",
                    "Click 'Recipe -> Add New Recipe'.\n" +
                            "Enter Title, Duration, Ingredients, Instructions, Image and click 'Save'.");

            TitledPane editRecipe = createSection("✏️ 3. Editing a Recipe",
                    "Click a recipe image to open details.\n" +
                            "Click 'Edit', change info, and 'Save'.");

            TitledPane deleteRecipe = createSection("🗑 4. Deleting a Recipe",
                    "Open recipe detail.\n" +
                            "Click 'Delete' and confirm.");

            TitledPane searchFilter = createSection("🔍 5. Searching & Filtering",
                    "Use search bar for title or ingredient.\n" +
                            "Use checkboxes to filter by categories.");

            TitledPane sorting = createSection("🔃 6. Sorting Recipes",
                    "Use sort options above the grid to sort by Title, ID, or Duration.");

            Button backBtn = new Button("⬅ Back to Main Menu");
            backBtn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            backBtn.setOnAction(ev -> manualStage.close());

            contentBox.getChildren().addAll(title, overview, addRecipe_, editRecipe, deleteRecipe, searchFilter, sorting, backBtn);

            ScrollPane scrollPane = new ScrollPane(contentBox);
            scrollPane.setFitToWidth(true);

            Scene scene = new Scene(scrollPane, 640, 580);
            manualStage.setScene(scene);
            manualStage.show();
        });

        helpMenu.getItems().add(helpItem);

        // ------------------ About Menu ------------------
        Menu aboutMenu = new Menu("About");

        MenuItem aboutItem = new MenuItem("About Recipe Finder");
        aboutItem.setOnAction(e -> {
            Stage aboutStage = new Stage();
            aboutStage.setTitle("About Recipe Finder");

            Label appTitle = new Label("🍳 Recipe Finder App");
            appTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));

            Label version = new Label("Version 1.0");
            version.setFont(Font.font("Arial", 14));

            Label developer = new Label(
                    "Developed by Team:\n" +
                            "- Deniz KARAMAN (deniz.karaman@edu.rtu.lv)\n" +
                            "- Alexandre Olivier COUDEVYLLE (Alexandre-Olivier.Coudevylle@edu.rtu.lv)\n" +
                            "- Oscar Martin Henry Jean DEBACKER (oscar.debacker@edu.rtu.lv)\n" +
                            "- Jean-Marc BOURDELOIE (jean-marc.bourdeloie@edu.rtu.lv)\n" +
                            "- Arsalan KHAN (Arsalan.Khan@edu.rtu.lv)\n" +
                            "- Jakhongir FOZILOV (jakhongir.fozilov@edu.rtu.lv)"
            );
            developer.setFont(Font.font("Arial", 14));

            Label description = new Label(
                    "Recipe Finder allows you to browse, search, add, edit, delete, filter, and sort recipes.\n" +
                            "All your changes are saved in a JSON file for next time."
            );
            description.setFont(Font.font("Arial", 13));
            description.setWrapText(true);

            VBox layout = new VBox(10, appTitle, version, developer, description);
            layout.setAlignment(Pos.CENTER);
            layout.setPadding(new Insets(20));
            layout.setPrefWidth(550);

            Scene scene = new Scene(layout);
            aboutStage.initModality(Modality.APPLICATION_MODAL);
            aboutStage.setScene(scene);
            aboutStage.showAndWait();
        });

        aboutMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(recipeMenu, helpMenu, aboutMenu);

        return menuBar;
    }

    private void openAddRecipeDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Recipe");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(15));

        TextField titleField = new TextField();
        titleField.setPromptText("Recipe Title");

        TextField durationField = new TextField();
        durationField.setPromptText("Duration (minutes)");

        TextArea ingredientsArea = new TextArea();
        ingredientsArea.setPromptText("Ingredients (comma separated)");

        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("Instructions");

        // Image selection
        final String[] selectedImagePath = {null};

        Button imageButton = new Button("Select Image");
        Label imageLabel = new Label("No image selected");

        imageButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Recipe Image");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                selectedImagePath[0] = file.toURI().toString();
                imageLabel.setText("Image selected");
            }
        });

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                if (titleField.getText().trim().isEmpty()) {
                    showAlert("Error", null, "Title cannot be empty!");
                    return;
                }

                Recipe newRecipe = new Recipe();
                newRecipe.setId(recipes.size() + 1001);
                newRecipe.setTitle(titleField.getText().trim());
                newRecipe.setDuration(
                        Integer.parseInt(durationField.getText().trim())
                );
                newRecipe.setIngredients(
                        Arrays.stream(ingredientsArea.getText().split(","))
                                .map(String::trim)
                                .collect(Collectors.toList())
                );
                newRecipe.setInstructions(instructionsArea.getText().trim());

                newRecipe.setImageUrl(
                        selectedImagePath[0] != null
                                ? selectedImagePath[0]
                                : Recipe.DEFAULT_IMAGE_URL
                );

                recipes.add(newRecipe);
                filteredRecipes.add(newRecipe);
                RecipeSaver.saveRecipes(recipes);
                createRecipeGrid(filteredRecipes);
                dialog.close();

            } catch (NumberFormatException ex) {
                showAlert("Error", null, "Duration must be a number!");
            }
        });

        vbox.getChildren().addAll(
                new Label("Title:"), titleField,
                new Label("Duration:"), durationField,
                new Label("Ingredients:"), ingredientsArea,
                new Label("Instructions:"), instructionsArea,
                imageButton, imageLabel,
                saveButton
        );

        Scene scene = new Scene(vbox, 400, 480);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public HBox createSortBox() {
        ComboBox<String> sortOptions = new ComboBox<>();
        sortOptions.getItems().addAll(
                "Sort by Title (A-Z)",
                "Sort by Title (Z-A)",
                "Sort by ID (Ascending)",
                "Sort by ID (Descending)",
                "Sort by Duration (Ascending)",
                "Sort by Duration (Descending)"
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
                case "Sort by Duration (Ascending)" ->
                        recentRecipes.sort(Comparator.comparingInt(Recipe::getDuration));
                case "Sort by Duration (Descending)" ->
                        recentRecipes.sort(Comparator.comparingInt(Recipe::getDuration).reversed());
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
        searchBox.setAlignment(Pos.TOP_CENTER);
        searchBox.setPrefWidth(300);

        Label titleLabel = new Label("\uD83C\uDF74 RECIPE FINDER \uD83C\uDF74");
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

        VBox categoriesContainer = new VBox(8);
        categoriesContainer.setAlignment(Pos.TOP_LEFT);
        categoriesContainer.setPadding(new Insets(5));

        Map<String, List<String>> categoryMap = getDefaultIngredientCategories();

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

        searchButton.setOnAction(e -> {

            List<String> selectedIngredients = getSelectedIngredients();

            String searchText = searchField.getText().trim().toLowerCase();

            filteredRecipes = recipes.stream()
                    .filter(recipe -> {
                        // searchText match: check in title OR ingredients
                        boolean textMatch;
                        if (searchText.isEmpty()) {
                            textMatch = true;
                        } else {
                            String lowerSearch = searchText.toLowerCase();
                            boolean titleMatch = recipe.getTitle() != null && recipe.getTitle().toLowerCase().contains(lowerSearch);
                            boolean ingMatch = recipe.getIngredients().stream()
                                    .anyMatch(ing -> ing.toLowerCase().contains(lowerSearch));
                            textMatch = titleMatch || ingMatch;
                        }

                        // ingredients match (checkboxes)
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
        categoriesContainer.getChildren().addAll(accordion);

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
        openRecipeDetail(recipe, null);
    }

    private void openRecipeDetail(Recipe recipe, Stage existingStage) {
        Stage detailStage = (existingStage != null) ? existingStage : new Stage();

        String imageUrl = recipe.getImageUrl() == null || recipe.getImageUrl().isEmpty()
                ? "image.jpeg"
                : recipe.getImageUrl();

        Image image = new Image(imageUrl, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(350);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                Rectangle clip = new Rectangle(imageView.getBoundsInParent().getWidth(),
                        imageView.getBoundsInParent().getHeight());
                clip.setArcWidth(15);
                clip.setArcHeight(15);
                imageView.setClip(clip);
            }
        });

        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setWrapText(true);

        Label idLabel = new Label("ID: " + recipe.getId());
        Label durationLabel = new Label("Duration: " + recipe.getDuration() + " min");

        // Ingredients scrollable
        VBox ingredientsContent = new VBox(5);
        ingredientsContent.getChildren().add(new Label("Ingredients:"));
        for (String ing : recipe.getIngredients()) {
            ingredientsContent.getChildren().add(new Label("• " + ing));
        }

        ScrollPane ingredientsScroll = new ScrollPane(ingredientsContent);
        ingredientsScroll.setFitToWidth(true);
        ingredientsScroll.setPrefHeight(200);

        // Instructions
        TextArea instrArea = new TextArea(recipe.getInstructions());
        instrArea.setEditable(false);
        ScrollPane instrScroll = new ScrollPane(instrArea);
        instrScroll.setFitToWidth(true);
        instrScroll.setPrefHeight(150);

        // Buttons
        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        HBox buttonBox = new HBox(10, editButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER);

        // ----------- EDIT -------------
        editButton.setOnAction(e -> {

            TextField titleField = new TextField(recipe.getTitle());
            TextField durationField = new TextField(String.valueOf(recipe.getDuration()));
            TextArea ingredientsArea = new TextArea(String.join(", ", recipe.getIngredients()));
            TextArea instructionsArea = new TextArea(recipe.getInstructions());

            VBox editContent = new VBox(10,
                    imageView,
                    new Label("Title:"), titleField,
                    idLabel,
                    new Label("Duration:"), durationField,
                    new Label("Ingredients:"), ingredientsArea,
                    new Label("Instructions:"), instructionsArea
            );

            Button saveButton = new Button("Save");
            Button cancelButton = new Button("Cancel");

            HBox editButtons = new HBox(10, saveButton, cancelButton);
            editButtons.setAlignment(Pos.CENTER);

            editContent.getChildren().add(editButtons);

            detailStage.getScene().setRoot(editContent);

            saveButton.setOnAction(ev -> {
                try {
                    recipe.setTitle(titleField.getText().trim());
                    recipe.setDuration(Integer.parseInt(durationField.getText().trim()));
                    recipe.setIngredients(Arrays.stream(ingredientsArea.getText().split(","))
                            .map(String::trim).collect(Collectors.toList()));
                    recipe.setInstructions(instructionsArea.getText().trim());

                    RecipeSaver.saveRecipes(recipes);
                    createRecipeGrid(filteredRecipes);

                    openRecipeDetail(recipe, detailStage);

                } catch (NumberFormatException ex) {
                    showAlert("Error", null, "Duration must be a number!");
                }
            });

            cancelButton.setOnAction(ev2 -> {
                openRecipeDetail(recipe, detailStage);
            });
        });

        // ----------- DELETE -------------
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this recipe?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText(null);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    recipes.remove(recipe);
                    filteredRecipes.remove(recipe);
                    RecipeSaver.saveRecipes(recipes);
                    createRecipeGrid(filteredRecipes);
                    detailStage.close();
                }
            });
        });

        VBox vBox = new VBox(15,
                imageView,
                titleLabel,
                idLabel,
                durationLabel,
                ingredientsScroll,
                new Label("Instructions:"),
                instrScroll,
                buttonBox
        );

        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(20));

        Scene scene = new Scene(vBox, 550, 700);

        detailStage.setScene(scene);
        detailStage.setTitle(recipe.getTitle());

        if (existingStage == null) {
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.showAndWait();
        }
    }

    private static void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private Map<String, List<String>> getDefaultIngredientCategories() {
        Map<String, List<String>> m = new LinkedHashMap<>();

        m.put("Produce", Arrays.asList(
                "Tomato", "Onion", "Garlic", "Bell Pepper", "Cucumber",
                "Carrot", "Lettuce", "Spinach", "Broccoli",
                "Mushroom"
        ));

        m.put("Dairy & Eggs", Arrays.asList(
                "Milk", "Butter", "Cheese", "Eggs", "Yogurt", "Cream",
                "Mozzarella", "Parmesan", "Feta", "Cottage Cheese"
        ));

        m.put("Meat & Seafood", Arrays.asList(
                "Chicken", "Beef", "Pork", "Salmon", "Shrimp", "Bacon",
                "Turkey", "Tuna", "Lamb", "Cod"
        ));

        m.put("Pantry Staples", Arrays.asList(
                "Flour", "Sugar", "Salt", "Rice", "Olive Oil",
                "Vinegar", "Baking Powder", "Soy Sauce"
        ));

        m.put("Herbs & Spices", Arrays.asList(
                "Basil", "Oregano", "Parsley", "Black Pepper", "Chili Flakes",
                "Cumin", "Turmeric", "Paprika", "Rosemary", "Thyme"
        ));

        m.put("Sauces & Condiments", Arrays.asList(
                "Ketchup", "Mayonnaise", "Mustard", "BBQ Sauce",
                "Honey", "Salsa", "Pesto", "Tahini"
        ));

        m.put("Fruits", Arrays.asList(
                "Apple", "Banana", "Orange", "Strawberry", "Blueberry",
                "Lemon", "Lime", "Mango"
        ));

        return m;
    }

    private void seedSampleRecipes() {
        recipes.clear();
        recipes.addAll(RecipeLoader.loadRecipes());
        filteredRecipes = new ArrayList<>(recipes);
    }

    private TitledPane createSection(String title, String bodyText) {
        Label body = new Label(bodyText);
        body.setWrapText(true);
        body.setFont(Font.font("Arial", 13));
        body.setStyle("-fx-text-fill: #333333;");

        VBox container = new VBox(body);
        container.setPadding(new Insets(10));
        container.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-radius: 5;");

        TitledPane pane = new TitledPane(title, container);
        pane.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        pane.setExpanded(false);

        return pane;
    }
}