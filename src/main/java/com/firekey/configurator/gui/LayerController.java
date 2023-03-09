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
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Handling the layer and key-layer configuration
 */
public class LayerController implements Initializable {

    // region attributes
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
     * The current selected {@link Layer} object
     */
    private Layer currentLayer;

    /**
     * The current selected {@link Key} object inside the {@link #currentLayer}
     */
    private Key currentSelectedKey;

    // endregion

    /**
     * Updates the visuals for a selected key
     */
    private void setVisualsToKeyData() {
        if (currentSelectedKey == null) return;
        // We have a key selected so update the visuals
        tfKeyName.setText(currentSelectedKey.getName());
        taFunctionInput.setText(currentSelectedKey.getFunction());
        cpDefaultKeyColor.setValue(currentSelectedKey.getDefaultColor());

        if (currentSelectedKey.getType() != KeyType.ACTION) {
            // If the current selected key is not a action key, disable the name and function input
            tfKeyName.setEditable(false);
            taFunctionInput.setEditable(false);
        } else {
            tfKeyName.setEditable(true);
            taFunctionInput.setEditable(true);
        }
    }

    /**
     * Sets the current layer
     *
     * @param layer The selected {@link Layer} object
     */
    public void setLayer(Layer layer) {
        this.currentLayer = layer;

        // set default values for the key edit fields
        this.currentSelectedKey = null;
        tfKeyName.setText("");
        taFunctionInput.setText("");
        cpDefaultKeyColor.setValue(Color.WHITE);

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

        // Add auto complete items to the function text area
        // TODO add entries
        taFunctionInput.addAutoCompleteEntry("Keyboard.press();")
                .addAutoCompleteEntry("KEY_LEFT_CTRL")
                .addAutoCompleteEntry("if() { } else { }")
                .addAutoCompleteEntry("key->setLedRGB(255,255,255);")
                .addAutoCompleteEntry("key->setLedRGB(0,255,255,255);")
                .addAutoCompleteEntry("key->setLedRGB();")
                .addAutoCompleteEntry("key->getState();")
                .addAutoCompleteEntry("key->setState(false);")
                .addAutoCompleteEntry("key->setState(true);")
                .addAutoCompleteEntry("key->setLedDefault();")
                .addAutoCompleteEntry("key->setLedOff();")
                .addAutoCompleteEntry("key->setLedOn();");
    }
}
