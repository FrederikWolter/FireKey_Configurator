package com.firekey.configurator.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class LayerController {

    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);
        System.out.println(buttonIdx);
    }

}
