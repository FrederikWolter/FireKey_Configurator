package com.firekey.configurator;

import com.firekey.configurator.arduino.ArduinoCLI;
import com.firekey.configurator.config.Config;
import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.KeyType;
import com.firekey.configurator.config.Layer;
import com.firekey.configurator.gui.MainApplication;
import javafx.application.Application;
import javafx.scene.paint.Color;
import org.json.JSONObject;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Main entrypoint in this app.
 */
public class FireKey {
    // region attributes
    private static String dataPath;
    // endregion

    public static void main(String[] args) {
        try {
            dataPath = getDataPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        ArduinoCLI arduinoCLI = new ArduinoCLI(dataPath);

        try {
            arduinoCLI.init();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Key k = new Key("test", KeyType.ACTION, "Keyboard.press('x');\nKeyboard.press('y');", Color.GREEN);
        Layer l = new Layer("Layer1");
        l.setKey(0, k);
        Config c = new Config(50, 15, 10, 60, 50, dataPath);
        c.setLayer(0, l);

        JSONObject obj = c.toJSON();    // not part of the actual workflow
        try {
            c.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Config c2;
        try {
            c2 = new Config(dataPath).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //TODO HelloApplication Controller remove / rename -> FireKey Controller
        Application.launch(MainApplication.class, args);
    }

    private static String getDataPath() throws URISyntaxException {
        // TODO use System.getProperty("user.dir") instead?
        return new File(FireKey.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator) + File.separator;
    }

}
