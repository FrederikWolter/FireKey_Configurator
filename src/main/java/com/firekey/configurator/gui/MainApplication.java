package com.firekey.configurator.gui;

import com.firekey.configurator.FireKey;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        MainController controller = fxmlLoader.getController();
        Scene scene = new Scene(root);
        stage.setMinHeight(600.0 + 40);
        stage.setMinWidth(950.0 + 20);
        stage.setTitle("FireKey-Configurator " + FireKey.VERSION);
        stage.initStyle(StageStyle.DECORATED);      // TODO change to undecorated?
        stage.setScene(scene);

        stage.setOnCloseRequest(controller::onClose);

        stage.show();
    }
}