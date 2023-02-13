package com.firekey.configurator.gui;

import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

//TODO remove me / rename
public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    private TextArea cliOutput;

    private ArduinoCLI arduinoCLI;
    private String dataPath;

    /**
     * Init the {@link ArduinoCLI}
     *
     * @param dataPath The root resource path next to the jar.
     * @throws Exception TODO
     */
    public void initArduinoCLI(String dataPath) throws Exception {
        this.dataPath = dataPath;
        this.arduinoCLI = new ArduinoCLI(this.dataPath);
        this.arduinoCLI.init(cliOutput);
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }


}