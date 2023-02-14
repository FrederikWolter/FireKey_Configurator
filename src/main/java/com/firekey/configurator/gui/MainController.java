package com.firekey.configurator.gui;

import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

//TODO remove me / rename
public class MainController implements Initializable {
    private GridPane general;
    private GridPane command;
    private GridPane layer;

    @FXML
    private AnchorPane paneContent;
    @FXML
    private ToggleGroup tgNavigation;

    @FXML
    private TextArea cliOutput;

    private ArduinoCLI arduinoCLI;
    private String dataPath;

    /**
     * Init the {@link ArduinoCLI}
     *
     * @param dataPath The root resource path next to the jar.
     * @throws Exception TODO
     */
    public void initArduinoCLI(String dataPath) throws Exception {
        this.dataPath = dataPath;
        this.arduinoCLI = new ArduinoCLI(this.dataPath);
        this.arduinoCLI.init(cliOutput);
    }

    @FXML
    protected void onGeneralButtonClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(general);
    }

    @FXML
    protected void onCommandButtonClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(command);
    }

    @FXML
    protected void onLayerButtonClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(layer);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            general = FXMLLoader.load(getClass().getResource("general-view.fxml"));
            command = FXMLLoader.load(getClass().getResource("command-view.fxml"));
            layer   = FXMLLoader.load(getClass().getResource("layer-view.fxml"));
            onGeneralButtonClick();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }


}