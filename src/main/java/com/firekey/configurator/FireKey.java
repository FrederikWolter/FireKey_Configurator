package com.firekey.configurator;

import com.firekey.configurator.arduino.ArduinoCLI;
import com.firekey.configurator.config.Config;
import com.firekey.configurator.config.Key;
import com.firekey.configurator.config.KeyType;
import com.firekey.configurator.config.Layer;
import com.firekey.configurator.gui.HelloApplication;
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
            install(arduinoCLI);
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
        Application.launch(HelloApplication.class, args);
    }

    private static String getDataPath() throws URISyntaxException {
        // TODO use System.getProperty("user.dir") instead?
        return new File(FireKey.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator) + File.separator;
    }

    // region install
    // TODO Move to separate class? (Class Diagram defines it inside the FireKey-class)

    private static void install(ArduinoCLI arduinoCLI) throws Exception {
        // TODO create install status bar
        exportResource("arduino-cli.exe");
        exportResource("arduino-cli.yaml");
        exportResource("firmware/Config.h");
        exportResource("firmware/Debug.h");
        exportResource("firmware/Firmware.ino");
        exportResource("firmware/Key.h");

        arduinoCLI.init();
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName The name of the resource to copy
     * @throws Exception If the target file cant be found.
     * @see #exportResource(String, String)
     */
    public static void exportResource(String resourceName) throws Exception {
        exportResource(resourceName, resourceName);
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName TThe name of the resource to copy
     * @param targetName   The target output file
     * @throws Exception If the target file cant be found.
     * @see #dataPath
     */
    public static void exportResource(String resourceName, String targetName) throws Exception {
        File exportFile = new File(dataPath + resourceName);
        if (exportFile.exists()) {
            return;
        }
        try (InputStream stream = FireKey.class.getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file."); // TODO custom exception?
            }
            // create needed folders
            Path newFilePath = Path.of(dataPath + targetName);
            int fileNameSplitPos = newFilePath.toString().lastIndexOf(File.separator);
            Path folders = Path.of(newFilePath.toString().substring(0, fileNameSplitPos + 1));
            Files.createDirectories(folders);

            // copy file
            Files.copy(stream, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

    }

    // endregion

}
