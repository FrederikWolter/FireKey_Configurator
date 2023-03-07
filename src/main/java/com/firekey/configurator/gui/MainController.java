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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController implements Initializable {
    // region attributes
    private VBox general;
    private GridPane command;
    private GridPane layer;

    private ArduinoCLI arduinoCLI;
    private String dataPath;

    private String comPort;

    private LayerController layerController;
    private GeneralController generalController;

    private Config config;
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
        onCOMPortChanged();
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
    protected void onLayerClick(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int layerIdx = Integer.parseInt(data);

        layerController.setLayer(config.getLayer(layerIdx));

        paneContent.getChildren().clear();
        paneContent.getChildren().add(layer);
    }

    @FXML
    protected void onUploadFirmwareClick() {
        TextArea ta = (TextArea) command.lookup("#taCliOutput");    // TODO cleanup
        if (this.comPort != null && ta != null) {
            try {
                ta.appendText("Converting Config to Firmware Config...");
                config.toFirmware();
                ta.appendText("Done");
                arduinoCLI.upload(this.comPort, ta);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            // TODO Error
            if(ta != null){
                ta.appendText("No Port Selected!");
            }
        }
    }

    @FXML
    protected void onSaveConfigClick() {
        try {
            // TODO ask before save
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onResetConfigClick() {
        try {
            // TODO ask before reset
            config.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onCOMPortChanged() {
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
            //general = FXMLLoader.load(getClass().getResource("general-view.fxml"));
            FXMLLoader generalLoader = new FXMLLoader(getClass().getResource("general-view.fxml"));
            general = generalLoader.load();
            generalController = generalLoader.getController();

            command = FXMLLoader.load(getClass().getResource("command-view.fxml"));

            FXMLLoader layerLoader = new FXMLLoader(getClass().getResource("layer-view.fxml"));
            layer = layerLoader.load();
            layerController = layerLoader.getController();

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

        try {
            config = new Config(dataPath).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /*config = new Config(1, 2, 3, 4, 5, dataPath);//.load(); // TODO load
        Layer layer;
        // TODO remove
        for (int l = 0; l < Config.NUM_LAYERS; l++) {
            layer = new Layer("Layer" + l, config);
            for (int k = 0; k < Layer.NUM_KEYS; k++) {
                KeyType type;
                if (k < 12) {
                    type = KeyType.ACTION;
                } else if (k == 12) {
                    type = KeyType.NAV_UP;
                } else if (k == 13) {
                    type = KeyType.NAV_HOME;
                } else {
                    type = KeyType.NAV_DOWN;
                }

                Key key = new Key("A" + k + "L" + l, type, "", Color.rgb(0, 255, 0), config);
                layer.setKey(k, key);
            }
            config.setLayer(l, layer);
        }*/

        generalController.setConfig(config);

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }

    public void onClose(WindowEvent event) {
        if (config.hasChanged()) {
            // Create a confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setHeaderText("Do you want to save changes to your config before closing?");
            alert.setContentText("Choose your option.");

            // Add Save, Discard, and Cancel buttons to the dialog
            ButtonType saveButton = new ButtonType("Save");
            ButtonType discardButton = new ButtonType("Discard");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

            // Show the dialog and wait for the user to make a choice
            Optional<ButtonType> result = alert.showAndWait();

            if(result.isEmpty()){
                event.consume();
                return;
            }

            // Handle the user's choice
            if (result.get() == saveButton) {
                try {
                    config.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (result.get() == discardButton) {
                System.out.println("Don't save");
            } else {
                // User clicked Cancel or closed the dialog
                event.consume(); // Prevent the application from closing
            }
        }
    }
}