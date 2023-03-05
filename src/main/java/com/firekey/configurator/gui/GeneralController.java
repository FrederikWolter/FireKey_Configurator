package com.firekey.configurator.gui;

import com.firekey.configurator.config.Config;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeneralController implements Initializable {

    private Config config;

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

    public void setConfig(Config config) {
        this.config = config;
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
}
