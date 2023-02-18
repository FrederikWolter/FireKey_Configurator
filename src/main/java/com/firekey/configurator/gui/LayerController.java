package com.firekey.configurator.gui;

import com.firekey.configurator.gui.components.AutoCompleteTextArea;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class LayerController implements Initializable {

    @FXML
    private Pane layerPane;
    @FXML
    private AutoCompleteTextArea taFunctionInput;

    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);
        System.out.println(buttonIdx); // TODO remove
        System.out.println(getCurrentLayerIdx()); // TODO remove
    }

    private int getCurrentLayerIdx() {
        return (int) layerPane.getUserData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO add entries
        taFunctionInput.addAutoCompleteEntry("Keyboard.press();")
                .addAutoCompleteEntry("KEY_LEFT_CTRL")
                .addAutoCompleteEntry("if(){\n\n}");
    }
}
