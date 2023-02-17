package com.firekey.configurator.gui;

import com.firekey.configurator.FireKey;
import com.firekey.configurator.arduino.ArduinoCLI;
import com.firekey.configurator.config.Config;
import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.KeyType;
import com.firekey.configurator.config.Layer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
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

    private String comPort;
    // endregion

    @FXML
    private AnchorPane paneContent;
    @FXML
    private ToggleGroup tgNavigation;
    @FXML
    private ChoiceBox<String> cbPort;


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
        this.arduinoCLI.init(ta);  // TODO cool design pattern?
    }

    private void updateCOMPortChoiceBox(){
        cbPort.getItems().clear();
        cbPort.getItems().addAll(arduinoCLI.getPorts());
        for(String item : cbPort.getItems()){
            // TODO select FireKey if exist. else select first.
        }
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
        if (!matcher.find()) {
            return;
        }
        port = matcher.group();
        try {
            arduinoCLI.upload(port, ta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onCOMPortChanged(ActionEvent event){
        Pattern pattern = Pattern.compile("(COM[0-9])");
        Matcher matcher = pattern.matcher(cbPort.getValue());
        if (!matcher.find()) {
            return;
        }
        this.comPort = matcher.group();
    }

    // endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            this.dataPath = new File(FireKey.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator) + File.separator;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // TODO handling
        }

        try {
            general = FXMLLoader.load(getClass().getResource("general-view.fxml"));
            command = FXMLLoader.load(getClass().getResource("command-view.fxml"));
            layer = FXMLLoader.load(getClass().getResource("layer-view.fxml"));
            onGeneralClick();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        cbPort.setOnAction(this::onCOMPortChanged);

        try {
            this.initArduinoCLI(dataPath);
        } catch (Exception e) {
            throw new RuntimeException(e);  // TODO handling
        }

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }
}