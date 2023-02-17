package com.firekey.configurator.gui;

import com.firekey.configurator.FireKey;
import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

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
    private ComboBox<String> cbPort;


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

    private void updateCOMPortChoiceBox() {
        cbPort.getItems().clear();
        cbPort.getItems().addAll(arduinoCLI.getPorts());
        cbPort.getSelectionModel().select(getCBDefaultSelectionIdx());
    }

    private int getCBDefaultSelectionIdx() {
        if (!cbPort.getItems().isEmpty()) {
            for (int i = 0; i < cbPort.getItems().size(); i++) {
                Pattern pattern = Pattern.compile("FireKey \\(COM[0-9]\\)");
                Matcher matcher = pattern.matcher(cbPort.getItems().get(i));
                if (matcher.find()) {
                    return i;
                }
            }
            return 0;
        }
        return -1;
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
        TextArea ta = (TextArea) command.lookup("#taCliOutput");    // TODO cleanup
        if (this.comPort != null) {
            try {
                arduinoCLI.upload(this.comPort, ta);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // TODO Error
        }
    }

    @FXML
    protected void onCOMPortChanged(ActionEvent event) {
        if (cbPort.getValue() != null) {
            Pattern pattern = Pattern.compile("(COM[0-9])");
            Matcher matcher = pattern.matcher(cbPort.getValue());
            if (!matcher.find()) {
                return;
            }
            this.comPort = matcher.group();
        }
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

        try {
            this.initArduinoCLI(dataPath);
        } catch (Exception e) {
            throw new RuntimeException(e);  // TODO handling
        }

        updateCOMPortChoiceBox();
        // update items on open
        cbPort.addEventHandler(ComboBoxBase.ON_SHOWING, event -> updateCOMPortChoiceBox());

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }
}