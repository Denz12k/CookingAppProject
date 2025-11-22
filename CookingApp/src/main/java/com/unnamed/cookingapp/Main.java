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
import javafx.scene.text.TextAlignment;
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

        MenuBar menuBar = new MenuBar();
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
        Stage detailStage = new Stage();

        String imageUrl = recipe.getImageUrl();

        Image image = new Image(imageUrl, true);
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);  // fix width
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Clip, applied after image loads
        image.progressProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() == 1.0) {
                Rectangle clip = new Rectangle(imageView.getBoundsInParent().getWidth(),
                        imageView.getBoundsInParent().getHeight());
                clip.setArcWidth(15);
                clip.setArcHeight(15);
                imageView.setClip(clip);
            }
        });

        // Title and ID
        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.DARKBLUE);
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        Label idLabel = new Label("ID: " + recipe.getId());
        idLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        idLabel.setTextFill(Color.DARKGRAY);

        // Ingredients
        VBox ingredientsBox = new VBox(5);
        ingredientsBox.getChildren().add(new Label("Ingredients:"));
        for (String ing : recipe.getIngredients()) {
            Label ingLabel = new Label("• " + ing);
            ingLabel.setFont(Font.font("Arial", 14));
            ingredientsBox.getChildren().add(ingLabel);
        }

        // Instructions
        Label instrLabel = new Label("Instructions:");
        instrLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        TextArea instrArea = new TextArea(recipe.getInstructions() == null ? "" : recipe.getInstructions());
        instrArea.setWrapText(true);
        instrArea.setEditable(false);
        instrArea.setFont(Font.font("Arial", 14));
        instrArea.setPrefRowCount(8);
        instrArea.setStyle("-fx-control-inner-background: #f8f8f8; -fx-background-radius: 5;");

        // VBox for detail page
        VBox vBox = new VBox(15, imageView, titleLabel, idLabel, ingredientsBox, instrLabel, instrArea);
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(20));
        vBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        // Scene and Stage
        Scene scene = new Scene(vBox, 550, 650);
        detailStage.setTitle(recipe.getTitle());
        detailStage.setScene(scene);
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.showAndWait();
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
                "Carrot", "Potato", "Lettuce", "Spinach", "Broccoli",
                "Mushroom", "Zucchini"
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
                "Flour", "Sugar", "Salt", "Rice", "Pasta", "Olive Oil",
                "Vinegar", "Baking Powder", "Soy Sauce", "Canned Tomatoes",
                "Canned Beans", "Bread Crumbs"
        ));

        m.put("Herbs & Spices", Arrays.asList(
                "Basil", "Oregano", "Parsley", "Black Pepper", "Chili Flakes",
                "Cumin", "Turmeric", "Paprika", "Rosemary", "Thyme"
        ));

        m.put("Sauces & Condiments", Arrays.asList(
                "Ketchup", "Mayonnaise", "Mustard", "BBQ Sauce", "Hot Sauce",
                "Honey", "Soy Sauce", "Salsa", "Pesto", "Tahini"
        ));

        m.put("Nuts & Seeds", Arrays.asList(
                "Almonds", "Walnuts", "Cashews", "Sunflower Seeds", "Pumpkin Seeds",
                "Chia Seeds", "Peanuts", "Pecans"
        ));

        m.put("Fruits", Arrays.asList(
                "Apple", "Banana", "Orange", "Strawberry", "Blueberry",
                "Lemon", "Lime", "Mango", "Peach", "Pineapple"
        ));

        return m;
    }

    private void seedSampleRecipes() {
        if (!recipes.isEmpty()) return;

        // Sample recipes
        Recipe r1 = new Recipe();
        r1.setId(1001);
        r1.setTitle("Tomato Cucumber Salad");
        r1.setIngredients(Arrays.asList("Tomato", "Cucumber", "Olive Oil", "Salt", "Lemon"));
        r1.setInstructions("Chop tomato and cucumber. Mix with olive oil, salt and lemon. Serve chilled.");
        recipes.add(r1);

        Recipe r2 = new Recipe();
        r2.setId(1002);
        r2.setTitle("Garlic Butter Salmon");
        r2.setIngredients(Arrays.asList("Salmon", "Garlic", "Butter", "Salt", "Lemon"));
        r2.setInstructions("Sear salmon, add garlic butter sauce, bake for 10 minutes. Serve with lemon.");
        recipes.add(r2);

        Recipe r3 = new Recipe();
        r3.setId(1003);
        r3.setTitle("Pancakes");
        r3.setIngredients(Arrays.asList("Flour", "Milk", "Eggs", "Sugar", "Baking Powder", "Butter"));
        r3.setInstructions("Mix dry ingredients, add milk and eggs. Cook on griddle until golden.");
        recipes.add(r3);

        Recipe r4 = new Recipe();
        r4.setId(1004);
        r4.setTitle("Spaghetti Carbonara");
        r4.setIngredients(Arrays.asList("Spaghetti", "Eggs", "Pancetta", "Parmesan", "Black Pepper"));
        r4.setInstructions("Cook spaghetti. Fry pancetta. Mix eggs and parmesan. Combine all with pepper.");
        recipes.add(r4);

        Recipe r5 = new Recipe();
        r5.setId(1005);
        r5.setTitle("Chicken Caesar Salad");
        r5.setIngredients(Arrays.asList("Chicken Breast", "Romaine Lettuce", "Croutons", "Parmesan", "Caesar Dressing"));
        r5.setInstructions("Grill chicken, chop lettuce, add croutons, parmesan, and dressing. Toss well.");
        recipes.add(r5);

        Recipe r6 = new Recipe();
        r6.setId(1006);
        r6.setTitle("Chocolate Brownies");
        r6.setIngredients(Arrays.asList("Dark Chocolate", "Butter", "Sugar", "Eggs", "Flour"));
        r6.setInstructions("Melt chocolate and butter. Mix sugar and eggs. Combine all with flour and bake.");
        recipes.add(r6);

        Recipe r7 = new Recipe();
        r7.setId(1007);
        r7.setTitle("Avocado Toast");
        r7.setIngredients(Arrays.asList("Bread", "Avocado", "Lemon", "Salt", "Pepper"));
        r7.setInstructions("Toast bread, mash avocado with lemon, salt and pepper, spread on toast.");
        recipes.add(r7);

        Recipe r8 = new Recipe();
        r8.setId(1008);
        r8.setTitle("Beef Tacos");
        r8.setIngredients(Arrays.asList("Taco Shells", "Ground Beef", "Cheese", "Lettuce", "Salsa"));
        r8.setInstructions("Cook beef with spices, fill taco shells with beef, lettuce, cheese, and salsa.");
        recipes.add(r8);

        Recipe r9 = new Recipe();
        r9.setId(1009);
        r9.setTitle("Vegetable Stir Fry");
        r9.setIngredients(Arrays.asList("Broccoli", "Carrot", "Bell Pepper", "Soy Sauce", "Garlic"));
        r9.setInstructions("Stir fry vegetables with garlic and soy sauce over high heat for 5-7 minutes.");
        recipes.add(r9);

        Recipe r10 = new Recipe();
        r10.setId(1010);
        r10.setTitle("Lemon Cheesecake");
        r10.setIngredients(Arrays.asList("Cream Cheese", "Sugar", "Eggs", "Lemon", "Graham Crackers"));
        r10.setInstructions("Mix cream cheese, sugar, eggs, and lemon. Pour on crust and bake until set.");
        recipes.add(r10);

        // Additional 20 recipes
        Recipe r11 = new Recipe();
        r11.setId(1011);
        r11.setTitle("Grilled Cheese Sandwich");
        r11.setIngredients(Arrays.asList("Bread", "Cheddar Cheese", "Butter"));
        r11.setInstructions("Butter bread, add cheese, grill until golden.");
        recipes.add(r11);

        Recipe r12 = new Recipe();
        r12.setId(1012);
        r12.setTitle("Greek Salad");
        r12.setIngredients(Arrays.asList("Tomato", "Cucumber", "Feta", "Olives", "Olive Oil", "Onion"));
        r12.setInstructions("Chop vegetables, add feta and olives, drizzle with olive oil.");
        recipes.add(r12);

        Recipe r13 = new Recipe();
        r13.setId(1013);
        r13.setTitle("Caprese Salad");
        r13.setIngredients(Arrays.asList("Tomato", "Mozzarella", "Basil", "Olive Oil", "Salt"));
        r13.setInstructions("Layer tomato and mozzarella slices, add basil, drizzle with olive oil.");
        recipes.add(r13);

        Recipe r14 = new Recipe();
        r14.setId(1014);
        r14.setTitle("Banana Smoothie");
        r14.setIngredients(Arrays.asList("Banana", "Milk", "Honey", "Ice"));
        r14.setInstructions("Blend all ingredients until smooth.");
        recipes.add(r14);

        Recipe r15 = new Recipe();
        r15.setId(1015);
        r15.setTitle("Veggie Omelette");
        r15.setIngredients(Arrays.asList("Eggs", "Onion", "Bell Pepper", "Spinach", "Cheese"));
        r15.setInstructions("Beat eggs, add vegetables, cook on skillet, fold, and serve.");
        recipes.add(r15);

        Recipe r16 = new Recipe();
        r16.setId(1016);
        r16.setTitle("Shrimp Fried Rice");
        r16.setIngredients(Arrays.asList("Rice", "Shrimp", "Eggs", "Peas", "Soy Sauce"));
        r16.setInstructions("Stir fry shrimp, add rice, eggs, peas, and soy sauce until heated.");
        recipes.add(r16);

        Recipe r17 = new Recipe();
        r17.setId(1017);
        r17.setTitle("BBQ Ribs");
        r17.setIngredients(Arrays.asList("Pork Ribs", "BBQ Sauce", "Salt", "Pepper"));
        r17.setInstructions("Season ribs, bake or grill, brush with BBQ sauce until caramelized.");
        recipes.add(r17);

        Recipe r18 = new Recipe();
        r18.setId(1018);
        r18.setTitle("Margarita Pizza");
        r18.setIngredients(Arrays.asList("Pizza Dough", "Tomato Sauce", "Mozzarella", "Basil"));
        r18.setInstructions("Top dough with sauce, cheese, and basil, bake until crust is golden.");
        recipes.add(r18);

        Recipe r19 = new Recipe();
        r19.setId(1019);
        r19.setTitle("French Toast");
        r19.setIngredients(Arrays.asList("Bread", "Eggs", "Milk", "Sugar", "Cinnamon"));
        r19.setInstructions("Dip bread in egg mixture, fry until golden, sprinkle with sugar and cinnamon.");
        recipes.add(r19);

        Recipe r20 = new Recipe();
        r20.setId(1020);
        r20.setTitle("Spicy Tuna Roll");
        r20.setIngredients(Arrays.asList("Sushi Rice", "Nori", "Tuna", "Sriracha", "Cucumber"));
        r20.setInstructions("Roll tuna with rice, nori, cucumber, and spicy mayo, slice and serve.");
        recipes.add(r20);

        Recipe r21 = new Recipe();
        r21.setId(1021);
        r21.setTitle("Chicken Fajitas");
        r21.setIngredients(Arrays.asList("Chicken", "Bell Pepper", "Onion", "Tortilla", "Spices"));
        r21.setInstructions("Cook chicken and vegetables with spices, serve in tortillas.");
        recipes.add(r21);

        Recipe r22 = new Recipe();
        r22.setId(1022);
        r22.setTitle("Chocolate Chip Cookies");
        r22.setIngredients(Arrays.asList("Flour", "Sugar", "Butter", "Eggs", "Chocolate Chips"));
        r22.setInstructions("Mix ingredients, drop onto baking tray, bake until golden.");
        recipes.add(r22);

        Recipe r23 = new Recipe();
        r23.setId(1023);
        r23.setTitle("Caprese Skewers");
        r23.setIngredients(Arrays.asList("Cherry Tomatoes", "Mozzarella Balls", "Basil", "Olive Oil"));
        r23.setInstructions("Skewer tomatoes, mozzarella, and basil. Drizzle with olive oil.");
        recipes.add(r23);

        Recipe r24 = new Recipe();
        r24.setId(1024);
        r24.setTitle("Egg Fried Rice");
        r24.setIngredients(Arrays.asList("Rice", "Eggs", "Onion", "Peas", "Soy Sauce"));
        r24.setInstructions("Cook onions, scramble eggs, add rice and peas, stir fry with soy sauce.");
        recipes.add(r24);

        Recipe r25 = new Recipe();
        r25.setId(1025);
        r25.setTitle("Mushroom Risotto");
        r25.setIngredients(Arrays.asList("Arborio Rice", "Mushroom", "Onion", "Parmesan", "Butter"));
        r25.setInstructions("Cook onions, add rice and mushrooms, gradually add broth, finish with parmesan.");
        recipes.add(r25);

        Recipe r26 = new Recipe();
        r26.setId(1026);
        r26.setTitle("Greek Yogurt Parfait");
        r26.setIngredients(Arrays.asList("Greek Yogurt", "Granola", "Honey", "Berries"));
        r26.setInstructions("Layer yogurt, granola, and berries in a glass. Drizzle honey on top.");
        recipes.add(r26);

        Recipe r27 = new Recipe();
        r27.setId(1027);
        r27.setTitle("Beef Burger");
        r27.setIngredients(Arrays.asList("Burger Bun", "Ground Beef", "Cheese", "Lettuce", "Tomato"));
        r27.setInstructions("Cook beef patty, assemble burger with bun, cheese, lettuce, and tomato.");
        recipes.add(r27);

        Recipe r28 = new Recipe();
        r28.setId(1028);
        r28.setTitle("Veggie Wrap");
        r28.setIngredients(Arrays.asList("Tortilla", "Lettuce", "Tomato", "Cucumber", "Hummus"));
        r28.setInstructions("Spread hummus on tortilla, add vegetables, roll and slice.");
        recipes.add(r28);

        Recipe r29 = new Recipe();
        r29.setId(1029);
        r29.setTitle("Fruit Salad");
        r29.setIngredients(Arrays.asList("Apple", "Orange", "Banana", "Grapes", "Honey"));
        r29.setInstructions("Chop fruits, mix, drizzle honey on top, serve chilled.");
        recipes.add(r29);

        Recipe r30 = new Recipe();
        r30.setId(1030);
        r30.setTitle("Pumpkin Soup");
        r30.setIngredients(Arrays.asList("Pumpkin", "Onion", "Garlic", "Cream", "Salt", "Pepper"));
        r30.setInstructions("Cook pumpkin with onion and garlic, blend, add cream, season with salt and pepper.");
        recipes.add(r30);

        filteredRecipes = new ArrayList<>(recipes);
    }
}