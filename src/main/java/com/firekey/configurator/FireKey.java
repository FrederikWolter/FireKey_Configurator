package com.firekey.configurator;

import com.firekey.configurator.arduino.ArduinoCLI;
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
import java.util.List;

/**
 * Main entrypoint in this app.
 */
public class FireKey {

    public static void main(String[] args) {
        String dataPath;
        try {
            dataPath = getDataPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Key k = new Key("test", KeyType.Action, "Keyboard.press('x');\nKeyboard.press('y');", Color.GREEN);
        Layer l = new Layer("Layer1");
        l.setKey(0, k);
        Config c = null;
        c = new Config(50, 15, 10, 60, 50, dataPath);
        c.addLayer(0, l);
        JSONObject obj = c.toJSON();
        try {
            c.saveConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Config c2 = null;
        c2 = new Config(dataPath);

        try {
            c2.loadConfig();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ArduinoCLI arduinoCLI = new ArduinoCLI(dataPath);
        List<String> ports = arduinoCLI.getPorts();

        //TODO HelloApplication Controller remove / rename -> FireKey Controller
        Application.launch(HelloApplication.class, args);
    }

    private static String getDataPath() throws URISyntaxException {
        // TODO use System.getProperty("user.dir") instead?
        return new File(FireKey.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator);
    }

}
