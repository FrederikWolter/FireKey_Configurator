package com.firekey.configurator.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class LayerController {

    @FXML
    private Pane layerPane;

    @FXML
    protected void onMatrixButtonClicked(ActionEvent event) {
        Node node = (Node) event.getSource();
        String data = (String) node.getUserData();
        int buttonIdx = Integer.parseInt(data);
        System.out.println(buttonIdx); // TODO remove
        System.out.println(getCurrentLayerIdx()); // TODO remove
    }

    private int getCurrentLayerIdx(){
        return (int) layerPane.getUserData();
    }

}
