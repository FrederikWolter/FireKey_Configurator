package com.firekey.configurator.gui;

import com.firekey.configurator.FireKey;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MainApplication extends Application {

    // region attributes
    private final String dataPath;
    // endregion

    public MainApplication() {
        try {
            dataPath = getDataPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        MainController controller = fxmlLoader.getController();
        try {
            controller.initArduinoCLI(dataPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Scene scene = new Scene(root);     // TODO use 900, 600?
        stage.setTitle("Hello FireKey!");               // TODO change title
        stage.initStyle(StageStyle.DECORATED);      // TODO change to undecorated?
        stage.setScene(scene);
        stage.show();
    }

    private static String getDataPath() throws URISyntaxException {
        // TODO use System.getProperty("user.dir") instead?
        return new File(FireKey.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator) + File.separator;
    }
}