package com.firekey.configurator;

import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.KeyType;
import com.firekey.configurator.gui.HelloApplication;
import javafx.application.Application;
import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.paint.Color;
import org.json.JSONObject;

/**
 * Main entrypoint in this app.
 */
public class FireKey {

    public static void main(String[] args) {
        SerialPort [] ports = SerialPort.getCommPorts();
        Key k = new Key("test", KeyType.Action, "Keyboard.press('x');\nKeyboard.press('y');", Color.GREEN);
        JSONObject j = k.toJSON();

        //TODO HelloApplication Controller remove / rename -> FireKey Controller
        Application.launch(HelloApplication.class, args);
    }
}
