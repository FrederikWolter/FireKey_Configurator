package com.firekey.configurator.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

public class LayerController {

    private int currentLayerIdx;

    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);
        System.out.println(buttonIdx); // TODO remove
    }

    public void setLayerIndex(int layerIdx) {
        this.currentLayerIdx = layerIdx;
        System.out.println(this.currentLayerIdx); // TODO remove
    }
}
