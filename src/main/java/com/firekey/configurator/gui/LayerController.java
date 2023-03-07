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

public class LayerController implements Initializable {

    @FXML
    private AutoCompleteTextArea taFunctionInput;
    @FXML
    private ColorPicker cpDefaultKeyColor;
    @FXML
    private TextFieldWithLengthLimit tfKeyName;

    @FXML
    private TextFieldWithLengthLimit tfLayerName;

    private Layer currentLayer;
    private Key currentSelectedKey;

    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);
        currentSelectedKey = currentLayer.getKey(buttonIdx);
        setVisualsToKeyData();
    }

    private void setVisualsToKeyData() {
        if (currentSelectedKey == null) return;
        tfKeyName.setText(currentSelectedKey.getName());
        taFunctionInput.setText(currentSelectedKey.getFunction());
        cpDefaultKeyColor.setValue(currentSelectedKey.getDefaultColor());
        if (currentSelectedKey.getType() != KeyType.ACTION) {
            tfKeyName.setEditable(false);
            taFunctionInput.setEditable(false);
        } else {
            tfKeyName.setEditable(true);
            taFunctionInput.setEditable(true);
        }
    }

    public void setLayer(Layer layer) {
        this.currentLayer = layer;
        // TODO reset visuals correct
        tfKeyName.setText("");
        taFunctionInput.setText("");
        cpDefaultKeyColor.setValue(Color.WHITE);
        tfLayerName.setText(currentLayer.getName());
    }

    protected void onFunctionTextChanged() {
        if (currentSelectedKey == null || currentSelectedKey.getType() != KeyType.ACTION) return;
        this.currentSelectedKey.setFunction(taFunctionInput.getText());
    }

    @FXML
    protected void onColorChanged() {
        if (currentSelectedKey == null) return;
        this.currentSelectedKey.setDefaultColor(cpDefaultKeyColor.getValue());
    }

    protected void onKeyNameChanged() {
        if (currentSelectedKey == null || currentSelectedKey.getType() != KeyType.ACTION) return;
        this.currentSelectedKey.setName(tfKeyName.getText());
    }

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


        // TODO add entries
        taFunctionInput.addAutoCompleteEntry("Keyboard.press();")
                .addAutoCompleteEntry("KEY_LEFT_CTRL")
                .addAutoCompleteEntry("if(){\n\n}")
                .addAutoCompleteEntry("else {\n\n}")
                .addAutoCompleteEntry("if(){\n\n} else {\n\n}")
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
