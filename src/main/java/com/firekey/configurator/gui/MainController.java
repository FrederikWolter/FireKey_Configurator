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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

        layerController.setLayer(layerIdx, config.getLayer(layerIdx));

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
    protected void onSaveConfigClick() {
        try {
            config.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onResetConfigClick() {
        try {
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

        config = new Config(1, 2, 3, 4, 5, dataPath);//.load(); // TODO load
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
        }

        generalController.setConfig(config);

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }
}