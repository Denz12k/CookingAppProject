module com.unnamed.cookingapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.google.gson;

    opens com.unnamed.cookingapp to com.google.gson;

    exports com.unnamed.cookingapp;
}