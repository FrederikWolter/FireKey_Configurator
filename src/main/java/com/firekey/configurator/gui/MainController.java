package com.firekey.configurator.gui;

import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO remove me / rename
public class MainController implements Initializable {
    // region attributes
    private GridPane general;
    private GridPane command;
    private GridPane layer;

    private ArduinoCLI arduinoCLI;
    private String dataPath;
    // endregion

    @FXML
    private AnchorPane paneContent;
    @FXML
    private ToggleGroup tgNavigation;


    /**
     * Init the {@link ArduinoCLI}
     *
     * @param dataPath The root resource path next to the jar.
     * @throws Exception TODO
     */
    public void initArduinoCLI(String dataPath) throws Exception {
        this.dataPath = dataPath;
        this.arduinoCLI = new ArduinoCLI(this.dataPath);
        TextArea ta = (TextArea) command.lookup("#taCliOutput");
        //this.arduinoCLI.init(ta);  // TODO cool design pattern?
    }

    // region listener
    @FXML
    protected void onGeneralClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(general);
    }

    @FXML
    protected void onCommandClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(command);
    }

    @FXML
    protected void onLayerClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(layer);
    }

    @FXML
    protected void onUploadFirmwareClick() {
        // TODO get correct port
        TextArea ta = (TextArea) command.lookup("#taCliOutput");    // TODO cleanup
        String port = arduinoCLI.getPorts().get(0);
        Pattern pattern = Pattern.compile("(COM[0-9])");
        Matcher matcher = pattern.matcher(port);
        if (!matcher.find())
        {
            return;
        }
        port = matcher.group();
        try {
            arduinoCLI.upload(port, ta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    // endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            general = FXMLLoader.load(getClass().getResource("general-view.fxml"));
            command = FXMLLoader.load(getClass().getResource("command-view.fxml"));
            layer   = FXMLLoader.load(getClass().getResource("layer-view.fxml"));
            onGeneralClick();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }
}