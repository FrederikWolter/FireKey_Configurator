package com.firekey.configurator.gui;

import com.firekey.configurator.config.Config;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;

public class GeneralController implements Initializable {

    // region attributes
    /**
     * The currently edited configuration-object of the firmware
     */
    private Config config;
    //endregion

    //region javafx-elements
    @FXML
    private Label lbSpamDelay;
    @FXML
    private Slider sliderSpamDelay;
    @FXML
    private Label lbHoldDelay;
    @FXML
    private Slider sliderHoldDelay;
    @FXML
    private Label lbDebounceDelay;
    @FXML
    private Slider sliderDebounceDelay;
    @FXML
    private Label lbSleepDelay;
    @FXML
    private Slider sliderSleepDelay;
    @FXML
    private Label lbLEDBright;
    @FXML
    private Slider sliderLEDBright;
    // endregion

    /**
     * Updates all sliders
     */
    public void updateVisuals() {
        sliderSpamDelay.setValue(config.getSpamDelay());
        sliderHoldDelay.setValue(config.getHoldDelay());
        sliderDebounceDelay.setValue(config.getDebounceDelay());
        sliderSleepDelay.setValue(config.getSleepDelay());
        sliderLEDBright.setValue(config.getLedBright());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sliderSpamDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = (int) sliderSpamDelay.getValue();
            config.setSpamDelay(value);
            lbSpamDelay.setText(lbSpamDelay.getText().replaceAll("\\((.*)\\)", "(" + value + ")"));
        });
        sliderHoldDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = (int) sliderHoldDelay.getValue();
            config.setHoldDelay(value);
            lbHoldDelay.setText(lbHoldDelay.getText().replaceAll("\\((.*)\\)", "(" + value + ")"));
        });
        sliderDebounceDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = (int) sliderDebounceDelay.getValue();
            config.setDebounceDelay(value);
            lbDebounceDelay.setText(lbDebounceDelay.getText().replaceAll("\\((.*)\\)", "(" + value + ")"));
        });
        sliderSleepDelay.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = (int) sliderSleepDelay.getValue();
            config.setSleepDelay(value);
            lbSleepDelay.setText(lbSleepDelay.getText().replaceAll("\\((.*)\\)", "(" + value + ")"));
        });
        sliderLEDBright.valueProperty().addListener((observable, oldValue, newValue) -> {
            int value = (int) sliderLEDBright.getValue();
            config.setLedBright(value);
            lbLEDBright.setText(lbLEDBright.getText().replaceAll("\\((.*)\\)", "(" + value + ")"));
        });
    }

    /**
     * Sets the current active configuration
     *
     * @param config The config element
     */
    public void setConfig(Config config) {
        this.config = config;
        updateVisuals();
    }
}
