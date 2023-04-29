package com.firekey.configurator.gui;

import com.firekey.configurator.FireKey;
import com.firekey.configurator.arduino.ArduinoCLI;
import com.firekey.configurator.auxiliary.ICallBack;
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
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController implements Initializable {
    // region attributes
    /**
     * The {@link GeneralController} central view-element
     */
    private VBox general;
    /**
     * The {@link CommandController} central view-element
     */
    private GridPane command;
    /**
     * Object to the general-tab-controller
     */
    private GeneralController generalController;
    /**
     * The {@link LayerController} central view-element
     */
    private GridPane layer;
    /**
     * Object to the layer-tab-controller
     */
    private LayerController layerController;
    /**
     * The {@link ArduinoCLI}-object to handle compile and upload of the firmware
     */
    private ArduinoCLI arduinoCLI;
    /**
     * The working directory of this configurator-software
     */
    private String dataPath;
    /**
     * Holds the selected com-port
     */
    private String comPort;
    /**
     * The currently edited configuration-object of the firmware
     */
    private Config config;
    /**
     * True, if an upload process is running
     */
    private boolean uploading;
    // endregion

    // region javafx-elements
    @FXML
    private AnchorPane paneContent;
    @FXML
    private ToggleGroup tgNavigation;
    @FXML
    private ComboBox<String> cbPort;
    @FXML
    private ToggleButton tbGeneral;
    @FXML
    private ToggleButton tbCLI;
    // endregion

    /**
     * Init the {@link ArduinoCLI}
     *
     * @param dataPath The root resource path next to the jar.
     * @throws Exception If an I/O error occurs ({@link ProcessBuilder#start()}
     */
    public void initArduinoCLI(String dataPath) throws Exception {
        this.dataPath = dataPath;
        TextArea ta = (TextArea) command.lookup("#taCliOutput");
        this.arduinoCLI = new ArduinoCLI(this.dataPath).init(ta);
    }

    /**
     * Updates the entries of the dropdown-menu and tries to select an usb-device named "FireKey"
     */
    private void updateCOMPortChoiceBox() {
        cbPort.getItems().clear();
        cbPort.getItems().addAll(arduinoCLI.getPorts());
        cbPort.getSelectionModel().select(getCBDefaultSelectionIdx());
        onCOMPortChanged();
    }

    /**
     * Tries to select an usb-device named "FireKey". If it cant, the first Element will be selected.
     *
     * @return The entry-index of the {@link #cbPort}-ChoiceBox
     */
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
        // TODO cleanup
        TextArea ta = (TextArea) command.lookup("#taCliOutput");
        if (comPort != null && ta != null && !uploading && arduinoCLI.isInstalled()) {
            uploading = true;
            onCommandClick();
            tbCLI.setSelected(true);
            try {
                ta.appendText(">Converting config...\n");
                config.toFirmware();
                ta.appendText(">Done\n");
                arduinoCLI.upload(comPort, ta, () -> {
                    // On Finished
                    uploading = false;
                    ta.appendText(">Firmware was successfully uploaded to the unit with COM port " + comPort + ".\n");
                }, () -> {
                    // On Error
                    uploading = false;
                    ta.appendText(">An error occurred while uploading the firmware to the selected device.\n");
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (ta != null) {
                onCommandClick();
                tbCLI.setSelected(true);
                if (comPort == null)
                    ta.appendText(">No Port Selected!\n");
                if (uploading)
                    ta.appendText(">Already Uploading. Please Wait!\n");
                if (!arduinoCLI.isInstalled())
                    ta.appendText(">ArduinoCLI is not ready. Please Wait!\n");
            } else {
                if (comPort == null)
                    createInfoPupUp("Warning", "No device selected", "You have not selected a com-device!");
                if (uploading)
                    createInfoPupUp("Uploading", "Already uploading", "An upload process is already running please wait!");
                if (!arduinoCLI.isInstalled())
                    createInfoPupUp("Installing", "ArduinoCLI not ready", "ArduinoCLI is not ready. Please Wait!");
            }
        }
    }

    @FXML
    protected void onSaveConfigClick() {
        try {
            config.save();
            createInfoPupUp("Save Config", "Save Config", "The Config has been saved!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onResetConfigClick() {
        createConfirmationPopUp("Restore Config", "Restore Config", "Do you want to restore your Config?",
                () -> {
                    try {
                        config.load();
                        createInfoPupUp("Restored Config", "Restored Config", "The Config has been restored!");
                        generalController.updateVisuals();
                        onGeneralClick();
                        tbGeneral.setSelected(true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
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

            command = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("command-view.fxml")));

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

        /*try {
            config = new Config(dataPath).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/
        config = new Config(1, 2, 3, 4, 5, dataPath);//.load(); // TODO load
        Layer layer;
        // TODO remove
        for (int l = 0; l < Config.NUM_LAYERS; l++) {
            layer = new Layer("Layer" + l, config);
            for (int k = 0; k < Layer.NUM_KEYS; k++) {
                KeyType type;
                String name = "A" + k + "L" + l;
                if (k < 12) {
                    type = KeyType.ACTION;
                } else if (k == 12) {
                    type = KeyType.NAV_UP;
                    name = "Nav_Up";
                } else if (k == 13) {
                    type = KeyType.NAV_HOME;
                    name = "Nav_Home";
                } else {
                    type = KeyType.NAV_DOWN;
                    name = "Nav_Down";
                }

                Key key = new Key(name, type, "", Color.rgb(0, 255, 0), config);
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

    public void onClose(WindowEvent event) {
        if (config.hasChanged()) {
            createConfirmationPopUp("Save Changes", "Do you want to save changes to your config before closing?", "Choose your option.",
                    () -> {
                        // on save
                        try {
                            config.save();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    () -> {
                        // on discard
                        System.out.println("Don't save");
                    },
                    () -> {
                        // on abort
                        event.consume(); // Prevent the application from closing
                    });
        }
    }

    /**
     * Creates and opens a confirmation dialog window.
     *
     * @param title       The title of the dialog
     * @param headerText  The text inside the header of the dialog
     * @param contentText The content of the dialog
     * @param onTrigger   Action to handle, if the "yes" is pressed
     */
    private void createConfirmationPopUp(String title, String headerText, String contentText, ICallBack onTrigger) {
        createConfirmationPopUp(title, headerText, contentText, onTrigger, () -> {
        }, () -> {
        });
    }

    /**
     * Creates and opens a confirmation dialog window.
     *
     * @param title       The title of the dialog
     * @param headerText  The text inside the header of the dialog
     * @param contentText The content of the dialog
     * @param onTrigger   Action to handle, if the "yes" is pressed
     * @param onDiscard   Action to handle, if the "no" is pressed
     * @param onAbort     Action to handle, if the dialog gets aborted
     */
    private void createConfirmationPopUp(String title, String headerText, String contentText, ICallBack onTrigger, ICallBack onDiscard, ICallBack onAbort) {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Add Save, Discard, and Cancel buttons to the dialog
        ButtonType saveButton = new ButtonType("Yes");
        ButtonType discardButton = new ButtonType("No");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);

        // Show the dialog and wait for the user to make a choice
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isEmpty()) {
            onAbort.invoke();
            return;
        }

        // Handle the user's choice
        if (result.get() == saveButton) {
            onTrigger.invoke();
        } else if (result.get() == discardButton) {
            onDiscard.invoke();
        } else {
            onAbort.invoke();
        }
    }

    /**
     * Creates and opens an information dialog window.
     *
     * @param title       The title of the dialog
     * @param headerText  The text inside the header of the dialog
     * @param contentText The content of the dialog
     */
    private void createInfoPupUp(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.show();
    }
}