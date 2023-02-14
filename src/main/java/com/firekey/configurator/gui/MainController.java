package com.firekey.configurator.gui;

import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

//TODO remove me / rename
public class MainController implements Initializable {
    // region attributes
    private GridPane general;
    private GridPane command;
    private GridPane layer;

    private ArduinoCLI arduinoCLI;
    private String dataPath;
    // endregion

    @FXML
    private AnchorPane paneContent;
    @FXML
    private ToggleGroup tgNavigation;


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
        ta.appendText("Hello\n");
        ta.appendText("geht das?\n");

        new Thread(){
            @Override
            public void run() {
                AtomicInteger i = new AtomicInteger();
                while (i.get() < 50){
                    Platform.runLater(() -> ta.appendText("Ping " + i.getAndIncrement() + "\n") );
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //this.arduinoCLI.init(cliOutput);  // TODO cool design pattern?
    }

    // region listener
    @FXML
    protected void onGeneralButtonClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(general);
    }

    @FXML
    protected void onCommandButtonClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(command);
    }

    @FXML
    protected void onLayerButtonClick() {
        paneContent.getChildren().clear();
        paneContent.getChildren().add(layer);
    }
    // endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            general = FXMLLoader.load(getClass().getResource("general-view.fxml"));
            command = FXMLLoader.load(getClass().getResource("command-view.fxml"));
            layer   = FXMLLoader.load(getClass().getResource("layer-view.fxml"));
            onGeneralButtonClick();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // keep always one button in navigation selected
        tgNavigation.selectedToggleProperty().addListener((obsVal, oldVal, newVal) -> {
            if (newVal == null) oldVal.setSelected(true);
        });
    }


}