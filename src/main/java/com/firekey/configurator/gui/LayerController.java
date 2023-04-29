package com.firekey.configurator.gui;

import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.KeyType;
import com.firekey.configurator.config.Layer;
import com.firekey.configurator.gui.components.AutoCompleteTextArea;
import com.firekey.configurator.gui.components.TextFieldWithLengthLimit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Handling the layer and key-layer configuration
 */
public class LayerController implements Initializable {

    // region attributes
    /**
     * The current selected {@link Key} object inside the {@link #currentLayer}
     */
    private Key currentSelectedKey;
    /**
     * The current selected {@link Layer} object
     */
    private Layer currentLayer;
    // endregion

    // region javafx-elements
    /**
     * The function text area
     */
    @FXML
    private AutoCompleteTextArea taFunctionInput;
    /**
     * The default color picker
     */
    @FXML
    private ColorPicker cpDefaultKeyColor;
    /**
     * Text field for the key name
     */
    @FXML
    private TextFieldWithLengthLimit tfKeyName;

    /**
     * Text field for the layer name
     */
    @FXML
    private TextFieldWithLengthLimit tfLayerName;
    /**
     * The toggle group of the key matrix
     */
    @FXML
    private ToggleGroup tgKeyMatrix;
    /**
     * Button to save the color to the whole layer
     */
    @FXML
    private Button btnSaveColorToAll;

    // endregion

    /**
     * Updates the visuals for a selected key
     */
    private void setVisualsToKeyData() {
        if (currentSelectedKey == null) {
            tfKeyName.setText("");
            taFunctionInput.setText("");
            cpDefaultKeyColor.setValue(Color.WHITE);

            tfKeyName.setDisable(true);
            taFunctionInput.setDisable(true);
            cpDefaultKeyColor.setDisable(true);
            btnSaveColorToAll.setDisable(true);
            return;
        }
        // We have a key selected so update the visuals
        tfKeyName.setText(currentSelectedKey.getName());
        taFunctionInput.setText(currentSelectedKey.getFunction());
        cpDefaultKeyColor.setValue(currentSelectedKey.getDefaultColor());

        if (currentSelectedKey.getType() != KeyType.ACTION) {
            // If the current selected key is not an action key, disable the name and function input
            tfKeyName.setDisable(true);
            taFunctionInput.setDisable(true);
        } else {
            tfKeyName.setDisable(false);
            taFunctionInput.setDisable(false);
        }
        cpDefaultKeyColor.setDisable(false);
        btnSaveColorToAll.setDisable(false);
    }

    /**
     * Sets the current layer
     *
     * @param layer The selected {@link Layer} object
     */
    public void setLayer(Layer layer) {
        this.currentLayer = layer;

        // reset selects
        this.currentSelectedKey = null;
        this.tgKeyMatrix.selectToggle(null);

        // update visuals
        setVisualsToKeyData();

        // Set the layer name to the input field
        tfLayerName.setText(currentLayer.getName());
    }

    /**
     * Selects the current clicked key object
     */
    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        // Get the selected key index from the user data
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);

        // Set the selected index & update the visuals to this key
        currentSelectedKey = currentLayer.getKey(buttonIdx);
        setVisualsToKeyData();
    }

    /**
     * Updates the function for a selected key
     */
    protected void onFunctionTextChanged() {
        if (currentSelectedKey == null || currentSelectedKey.getType() != KeyType.ACTION) return;
        this.currentSelectedKey.setFunction(taFunctionInput.getText());
    }

    /**
     * Updates the default color for a selected key
     */
    @FXML
    protected void onColorChanged() {
        if (currentSelectedKey == null) return;
        this.currentSelectedKey.setDefaultColor(cpDefaultKeyColor.getValue());
    }

    /**
     * Updates the default color for all keys of the layer
     */
    @FXML
    protected void onSaveColorToAllClicked() {
        if (currentSelectedKey == null && currentLayer == null) return;
        this.currentLayer.setDefaultColorToAllKeys(cpDefaultKeyColor.getValue());
    }

    /**
     * Updates the name for a selected key
     */
    protected void onKeyNameChanged() {
        if (currentSelectedKey == null || currentSelectedKey.getType() != KeyType.ACTION) return;
        this.currentSelectedKey.setName(tfKeyName.getText());
    }

    /**
     * Updates the name for the selected layer
     */
    private void onLayerNameChanged() {
        if (currentLayer == null) return;
        this.currentLayer.setName(tfLayerName.getText());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO/CHECK: use focusedProperty and on focus lost?
        taFunctionInput.textProperty().addListener((observable, oldValue, newValue) -> onFunctionTextChanged());

        tfKeyName.textProperty().addListener((observable, oldValue, newValue) -> onKeyNameChanged());
        tfKeyName.setMaxLength(8);

        tfLayerName.textProperty().addListener((observable, oldValue, newValue) -> onLayerNameChanged());
        tfLayerName.setMaxLength(10);

        // keep always one button in navigation selected
        tgKeyMatrix.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null && currentSelectedKey != null) oldVal.setSelected(true);
        });

        // Add auto complete items to the function text area
        taFunctionInput
                // Keyboard-Lib
                .addAutoCompleteEntry("Keyboard.press();")
                .addAutoCompleteEntry("Keyboard.release();")
                .addAutoCompleteEntry("Keyboard.releaseAll();")
                .addAutoCompleteEntry("Keyboard.print();")
                .addAutoCompleteEntry("Keyboard.println();")
                // Modifiers
                .addAutoCompleteEntry("KEY_LEFT_CTRL")
                .addAutoCompleteEntry("KEY_LEFT_SHIFT")
                .addAutoCompleteEntry("KEY_LEFT_ALT")
                .addAutoCompleteEntry("KEY_LEFT_GUI")
                .addAutoCompleteEntry("KEY_RIGHT_CTRL")
                .addAutoCompleteEntry("KEY_RIGHT_SHIFT")
                .addAutoCompleteEntry("KEY_RIGHT_ALT")
                .addAutoCompleteEntry("KEY_RIGHT_GUI")
                // Num Block Keys
                .addAutoCompleteEntry("KEY_KP_SLASH")
                // Special Keys
                .addAutoCompleteEntry("KEY_RETURN")
                .addAutoCompleteEntry("KEY_BACKSPACE")
                .addAutoCompleteEntry("KEY_TAB")
                .addAutoCompleteEntry("KEY_INSERT")
                .addAutoCompleteEntry("KEY_DELETE")
                .addAutoCompleteEntry("KEY_ESC")
                // Arrow Keys
                .addAutoCompleteEntry("KEY_UP_ARROW")
                .addAutoCompleteEntry("KEY_DOWN_ARROW")
                .addAutoCompleteEntry("KEY_LEFT_ARROW")
                .addAutoCompleteEntry("KEY_RIGHT_ARROW")
                .addAutoCompleteEntry("KEY_KP_SLASH")
                // C-Helpers
                .addAutoCompleteEntry("if() { } else { }")
                // FireKey-Functions
                .addAutoCompleteEntry("key->setLedRGB(255,255,255);")
                .addAutoCompleteEntry("key->setLedRGB(0,255,255,255);")
                .addAutoCompleteEntry("key->setLedRGB();")
                .addAutoCompleteEntry("key->getState();")
                .addAutoCompleteEntry("key->setState(false);")
                .addAutoCompleteEntry("key->setState(true);")
                .addAutoCompleteEntry("key->setLedDefault();")
                .addAutoCompleteEntry("key->setLedOff();")
                .addAutoCompleteEntry("key->setLedOn();")
                // F-Keys
                .addAutoCompleteEntry("KEY_F1")
                .addAutoCompleteEntry("KEY_F2")
                .addAutoCompleteEntry("KEY_F3")
                .addAutoCompleteEntry("KEY_F4")
                .addAutoCompleteEntry("KEY_F5")
                .addAutoCompleteEntry("KEY_F6")
                .addAutoCompleteEntry("KEY_F7")
                .addAutoCompleteEntry("KEY_F8")
                .addAutoCompleteEntry("KEY_F9")
                .addAutoCompleteEntry("KEY_F10")
                .addAutoCompleteEntry("KEY_F11")
                .addAutoCompleteEntry("KEY_F12")
                .addAutoCompleteEntry("KEY_F13")
                .addAutoCompleteEntry("KEY_F14")
                .addAutoCompleteEntry("KEY_F15")
                .addAutoCompleteEntry("KEY_F16")
                .addAutoCompleteEntry("KEY_F17")
                .addAutoCompleteEntry("KEY_F18")
                .addAutoCompleteEntry("KEY_F19")
                .addAutoCompleteEntry("KEY_F20")
                .addAutoCompleteEntry("KEY_F21")
                .addAutoCompleteEntry("KEY_F22")
                .addAutoCompleteEntry("KEY_F23")
                .addAutoCompleteEntry("KEY_F24");
    }
}
