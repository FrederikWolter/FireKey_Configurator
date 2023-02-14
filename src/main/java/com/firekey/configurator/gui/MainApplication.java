package com.firekey.configurator.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

//TODO remove me / rename + update in Main
public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());     // TODO use 900, 600?
        stage.setTitle("Hello FireKey!");               // TODO change title
        stage.initStyle(StageStyle.DECORATED);      // TODO change to undecorated?
        stage.setScene(scene);
        stage.show();
    }
}