package com.firekey.configurator.gui;

import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.Layer;
import com.firekey.configurator.gui.components.AutoCompleteTextArea;
import com.firekey.configurator.gui.components.TextFieldWithLengthLimit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class LayerController implements Initializable {

    @FXML
    private AutoCompleteTextArea taFunctionInput;
    @FXML
    private ColorPicker cpDefaultKeyColor;
    @FXML
    private TextFieldWithLengthLimit tfKeyName;


    private int currentLayerIdx;

    private Layer currentLayer;
    private Key currentSelectedKey;

    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);
        System.out.println(buttonIdx); // TODO remove
        if (currentLayer != null) {
            currentSelectedKey = currentLayer.getKey(buttonIdx);
            // TODO update visuals
        }
    }

    public void setLayer(int layerIdx, Layer layer) {
        this.currentLayerIdx = layerIdx;
        this.currentLayer = layer;
        System.out.println(this.currentLayerIdx); // TODO remove
        // TODO reset visuals
    }

    protected void onFunctionTextChanged() {
        this.currentSelectedKey.setFunction(taFunctionInput.getText());
    }

    @FXML
    protected void onColorChanged() {
        this.currentSelectedKey.setDefaultColor(cpDefaultKeyColor.getValue());
    }

    @FXML
    protected void onKeyNameChanged() {
        this.currentSelectedKey.setName(tfKeyName.getText());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO/CHECK: use focusedProperty and on focus lost?
        taFunctionInput.textProperty().addListener((observable, oldValue, newValue) -> onFunctionTextChanged());
        tfKeyName.setMaxLength(8);


        // TODO add entries
        taFunctionInput.addAutoCompleteEntry("Keyboard.press();")
                .addAutoCompleteEntry("KEY_LEFT_CTRL")
                .addAutoCompleteEntry("if(){\n\n}")
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
