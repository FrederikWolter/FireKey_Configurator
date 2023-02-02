package com.firekey.configurator.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

//TODO remove me / rename
public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}