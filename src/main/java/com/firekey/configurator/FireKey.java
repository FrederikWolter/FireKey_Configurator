package com.firekey.configurator;

import com.firekey.configurator.config.Config;
import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.KeyType;
import com.firekey.configurator.config.Layer;
import com.firekey.configurator.gui.HelloApplication;
import javafx.application.Application;
import com.fazecast.jSerialComm.SerialPort;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Main entrypoint in this app.
 */
public class FireKey {

    public static void main(String[] args) {
        SerialPort[] ports = SerialPort.getCommPorts();
        Key k = new Key("test", KeyType.Action, "Keyboard.press('x');\nKeyboard.press('y');", Color.GREEN);
        Layer l = new Layer("Layer1");
        l.addKey(0, k);
        Config c = null;
        try {
            c = new Config(50, 15, 10, 60, 50);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        c.addLayer(0, l);
        JSONObject obj = c.toJSON();
        try {
            c.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Config c2 = null;
        try {
            c2 = new Config();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try {
            c2.loadConfig();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //TODO HelloApplication Controller remove / rename -> FireKey Controller
        Application.launch(HelloApplication.class, args);
    }

    public static String getDataPath() throws URISyntaxException {
        // TODO use System.getProperty("user.dir") instead?
        // TODO/CHECK: don't make static but pass it to config-obj,etc?
        return new File(FireKey.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator);
    }

}
