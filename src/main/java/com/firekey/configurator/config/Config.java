package com.firekey.configurator.config;

import com.firekey.configurator.FireKey;
import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a whole FireKey config including its {@link Layer}s and the corresponding {@link Key}.
 */
public class Config {
    // region constants
    /**
     * Number of available layers
     */
    public static final int NUM_LAYERS = 5;
    /**
     * Name of the custom config file
     */
    private static final String CONFIG_FILE_NAME = "firekey_config.json";
    /**
     * Name of the default config file
     */
    private static final String DEFAULT_CONFIG_FILE_NAME = "firekey_config_default.json";

    // region json-names
    /**
     * Name of the JSON-entry for the {@link #holdDelay}-field
     */
    public static final String HOLD_DELAY = "holdDelay";
    /**
     * Name of the JSON-entry for the {@link #debounceDelay}-field
     */
    public static final String DEBOUNCE_DELAY = "debounceDelay";
    /**
     * Name of the JSON-entry for the {@link #spamDelay}-field
     */
    public static final String SLEEP_DELAY = "sleepDelay";
    /**
     * Name of the JSON-entry for the {@link #ledBright}-field
     */
    public static final String LED_BRIGHT = "ledBright";
    /**
     * Name of the JSON-entry for the {@link #spamDelay}-field
     */
    public static final String SPAM_DELAY = "spamDelay";
    /**
     * Name of the JSON-entry for the {@link #layers}-array
     */
    public static final String LAYERS = "layers";
    // endregion

    // endregion

    // region attributes
    /**
     * Defines how long a key on the macro keyboard must be pressed before it is considered "pressed" <br>
     * In milliseconds!
     */
    private int holdDelay;
    /**
     * Defines how often a key is pressed logically when it is held down. <br>
     * In milliseconds!
     */
    private int spamDelay;
    /**
     * Defines the delay for software debouncing
     */
    private int debounceDelay;
    /**
     * Defines the timeout from when the macro keyboard switches to sleep mode. <br>
     * In seconds!
     */
    private int sleepDelay;
    /**
     * Defines the brightness of the LEDs. <br>
     * Between 0 and 255!
     */
    private int ledBright;
    /**
     * The array of {@link Layer}-Objects
     */
    private final Layer[] layers;
    /**
     * Defines the path to the working directory of the configurator
     */
    private final String dataPath;
    /**
     * True, if a {@link Key} or {@link Layer} has been changed
     */
    private boolean changed;
    // endregion

    /**
     * Creates a new config-object
     *
     * @param dataPath Path to the working directory of the configurator
     */
    public Config(String dataPath) throws Exception {
        this.layers = new Layer[NUM_LAYERS];
        this.dataPath = dataPath;
        this.spamDelay = -1;
        this.holdDelay = -1;
        this.debounceDelay = -1;
        this.sleepDelay = -1;
        this.ledBright = -1;
        this.exportDefaultConfig();
    }

    // TODO remove this constructor
    public Config(int spamDelay, int holdDelay, int debounceDelay, int sleepDelay, int ledBright, String dataPath) {
        this.spamDelay = spamDelay;
        this.holdDelay = holdDelay;
        this.debounceDelay = debounceDelay;
        this.sleepDelay = sleepDelay;
        this.ledBright = ledBright;
        this.layers = new Layer[NUM_LAYERS];
        this.dataPath = dataPath;
    }

    /**
     * Loads the default configuration file or the custom configuration file if available and adjusts the corresponding fields of this class. <br>
     * In addition, the necessary layer and key objects are created.
     *
     * @return This config object.
     * @throws IOException If no config file could be found.
     */
    public Config load() throws IOException {
        // init values
        this.spamDelay = -1;
        this.holdDelay = -1;
        this.debounceDelay = -1;
        this.sleepDelay = -1;
        this.ledBright = -1;

        JSONObject configJson = this.loadJsonConfig();

        // get config values
        this.setSpamDelay(configJson.getInt(SPAM_DELAY));
        this.setHoldDelay(configJson.getInt(HOLD_DELAY));
        this.setDebounceDelay(configJson.getInt(DEBOUNCE_DELAY));
        this.setSleepDelay(configJson.getInt(SLEEP_DELAY));
        this.setLedBright(configJson.getInt(LED_BRIGHT));

        // get layers
        JSONArray layerJSONArray = configJson.getJSONArray(LAYERS);
        for (int layerIdx = 0; layerIdx < NUM_LAYERS; layerIdx++) {
            // get current layer
            JSONObject layerJson = layerJSONArray.optJSONObject(layerIdx);
            if (layerJson != null) {
                // get layer values
                Layer layer = new Layer(layerJson.getString(Layer.NAME), this);

                // get keys
                JSONArray keyJSONArray = layerJson.getJSONArray(Layer.KEYS);
                for (int keyIdx = 0; keyIdx < Layer.NUM_KEYS; keyIdx++) {
                    // get current key
                    JSONObject keyJson = keyJSONArray.optJSONObject(keyIdx);
                    if (keyJson != null) {
                        Key key = new Key(keyJson.getString(Key.NAME), keyJson.getEnum(KeyType.class, Key.TYPE), keyJson.getString(Key.FUNCTION), Color.web(keyJson.getString(Key.DEFAULT_COLOR)), this);
                        // add key to layer
                        layer.setKey(keyIdx, key);
                    }
                }
                // add layer to config
                this.setLayer(layerIdx, layer);
            }
        }
        return this;
    }

    /**
     * Loads the default configuration file or the custom configuration file if available.
     *
     * @return A {@link File}-object of the found config file.
     * @throws IOException If no config file could be found.
     */
    private JSONObject loadJsonConfig() throws IOException {
        File configFile = new File(dataPath + CONFIG_FILE_NAME);

        if (!configFile.exists()) { // custom config exists?
            configFile = new File(dataPath + DEFAULT_CONFIG_FILE_NAME);
        }
        if (!configFile.exists()) { // config still exist?
            throw new IOException("No Config found");   //TODO own exception?
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            return new JSONObject(new JSONTokener(fis));
        }
    }

    /**
     * Saves this {@link Config} to the {@link #CONFIG_FILE_NAME}-file next to the jar-file.
     *
     * @throws IOException if the named file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     */
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(dataPath + CONFIG_FILE_NAME)) {
            fw.write(this.toJSON().toString(1));
            resetChanged();
        } catch (IOException e) {
            throw new IOException();    //TODO own exception?
        }
    }

    /**
     * Updates the Config.h using the current config data
     *
     * @throws IOException
     */
    public void toFirmware() throws IOException {
        replaceFirmwareConfigFile();
        File configFile = new File(dataPath + ArduinoCLI.FIRMWARE_DATA_PATH + "Config.h");

        if (configFile.exists()) {
            Path configPath = Path.of(configFile.getPath());
            Map<String, String> definitions = buildConfigDefinitionsMap();

            String configFileString = Files.readString(configPath);

            String updatedConfigFileString = replaceConfigData(configFileString, definitions);

            Files.writeString(configPath, updatedConfigFileString);
        }
    }

    /**
     * Build the config replacement map. <br>
     * Keys are the strings, which should be replaced with the value-string. <br>
     * E.g. 'SPAM_DELAY 15' : 'SPAM_DELAY 50' means replace the string 'SPAM_DELAY 15' with 'SPAM_DELAY 50'
     *
     * @return The config replacement map.
     */
    private Map<String, String> buildConfigDefinitionsMap() {
        Map<String, String> definitions = new HashMap<>();
        addGeneralDefinitions(definitions);
        addLayerDefinitions(definitions);
        return definitions;
    }

    /**
     * Add the general information to the definitions map
     *
     * @param definitions The definitions-map
     */
    private void addGeneralDefinitions(Map<String, String> definitions) {
        definitions.put("SPAM_DELAY 15", "SPAM_DELAY " + this.getSpamDelay());
        definitions.put("HOLD_DELAY 100", "HOLD_DELAY " + this.getHoldDelay());
        definitions.put("DEBOUNCE_DELAY 10", "DEBOUNCE_DELAY " + this.getDebounceDelay());
        definitions.put("SLEEP_DELAY 60", "SLEEP_DELAY " + this.getSleepDelay());
        definitions.put("LED_BRIGHT 64", "LED_BRIGHT " + this.getLedBright());
    }

    /**
     * Add the layer information to the definitions map
     *
     * @param definitions The definitions-map
     */
    private void addLayerDefinitions(Map<String, String> definitions) {
        for (int layerIdx = 0; layerIdx < NUM_LAYERS; layerIdx++) {
            Layer layer = this.getLayer(layerIdx);
            if (layer != null) {
                // update layer name
                definitions.put("Layer" + layerIdx, layer.getName());   // placeholders are Layer0, Layer1, ...
                addKeyDefinitions(definitions, layerIdx, layer);
            }
        }
    }

    /**
     * Add the key information to the definitions map
     *
     * @param definitions The definitions-map
     * @param layerIdx    The current layer index
     * @param layer       The current layer
     */
    private void addKeyDefinitions(Map<String, String> definitions, int layerIdx, Layer layer) {
        for (int keyIdx = 0; keyIdx < Layer.NUM_KEYS; keyIdx++) {
            Key key = layer.getKey(keyIdx);
            if (key != null) {
                // add key function name
                if (key.getType() != KeyType.NAV_DOWN && key.getType() != KeyType.NAV_UP && key.getType() != KeyType.NAV_HOME)  // we should not change the navigation texts
                    definitions.put("L" + layerIdx + "K" + keyIdx, key.getName());  // placeholders are e.g. L0K0 for: layer 0 key 0

                // add default color
                int red = (int) (key.getDefaultColor().getRed() * 255);
                int green = (int) (key.getDefaultColor().getGreen() * 255);
                int blue = (int) (key.getDefaultColor().getBlue() * 255);
                definitions.put("{ 0, " + layerIdx + ", " + keyIdx + " }", "{ " + red + ", " + green + ", " + blue + " }"); // placeholders corresponding to sequential number sequences

                // add function
                if (key.getType() != KeyType.NAV_DOWN && key.getType() != KeyType.NAV_UP && key.getType() != KeyType.NAV_HOME) // we should not change the navigation functions
                    definitions.put("//K" + (keyIdx + 1) + "L" + layerIdx, key.getFunction());
            }
        }
    }

    /**
     * Replaces in a given string all strings based on the definitions map.
     * E.g. 'SPAM_DELAY 15' : 'SPAM_DELAY 50' means replace the string 'SPAM_DELAY 15' with 'SPAM_DELAY 50'
     * <a href='https://stackoverflow.com/questions/1326682/java-replacing-multiple-different-substring-in-a-string-at-once-or-in-the-most/40836618#40836618'>Code from Stackoverflow</a>
     *
     * @param text        The text, in which the strings should be replaced
     * @param definitions The string replacement map
     * @return The updated input text
     */
    private String replaceConfigData(final String text, final Map<String, String> definitions) {
        final String[] keys = definitions.keySet().toArray(new String[0]);
        final String[] values = definitions.values().toArray(new String[0]);

        return StringUtils.replaceEach(text, keys, values);
    }

    /**
     * Replaces the Config.h-file inside the {@link #dataPath} with Config_default.h
     *
     * @throws IOException {@link  Files#copy(Path, Path, CopyOption...)}
     */
    private void replaceFirmwareConfigFile() throws IOException {
        if (!new File(dataPath + ArduinoCLI.FIRMWARE_DATA_PATH + "Config_default.h").exists()) {
            return; // TODO error
        }
        // TODO/CHECK: instead of copy the file inside the data path, copy it from the resources?
        Files.copy(Path.of(dataPath + ArduinoCLI.FIRMWARE_DATA_PATH + "Config_default.h"), Path.of(dataPath + ArduinoCLI.FIRMWARE_DATA_PATH + "Config.h"), StandardCopyOption.REPLACE_EXISTING);
    }

    private void exportDefaultConfig() throws Exception {
        try (InputStream stream = FireKey.class.getResourceAsStream(DEFAULT_CONFIG_FILE_NAME)) {
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + DEFAULT_CONFIG_FILE_NAME + "\" from Jar file."); // TODO custom exception?
            }
            // copy file
            Files.copy(stream, Path.of(dataPath + DEFAULT_CONFIG_FILE_NAME), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Converts this {@link Config} into a {@link JSONObject} to save it to config-file.
     * Containing all {@link Layer}s in a {@link JSONArray}.
     *
     * @return The corresponding {@link JSONObject}
     * @see Layer#toJSON()
     */
    public JSONObject toJSON() {
        // convert all layers
        JSONArray layersJson = new JSONArray();
        for (Layer l : this.layers) {
            if (l != null) layersJson.put(l.toJSON());
        }

        return new JSONObject().put(SPAM_DELAY, this.spamDelay).put(HOLD_DELAY, this.holdDelay).put(DEBOUNCE_DELAY, this.debounceDelay).put(SLEEP_DELAY, this.sleepDelay).put(LED_BRIGHT, this.ledBright).put(LAYERS, layersJson);
    }

    // region getter
    public int getSpamDelay() {
        return spamDelay;
    }

    public int getHoldDelay() {
        return holdDelay;
    }

    public int getDebounceDelay() {
        return debounceDelay;
    }

    public int getSleepDelay() {
        return sleepDelay;
    }

    public int getLedBright() {
        return ledBright;
    }

    public Layer getLayer(int idx) {
        if (0 <= idx && idx < NUM_LAYERS) return this.layers[idx];
        return null;
    }

    /**
     * @return True, if a {@link Key} or {@link Layer} has been changed
     */
    public boolean hasChanged() {
        return changed;
    }
    // endregion

    // region setter
    public void setSpamDelay(int spamDelay) {
        if (this.spamDelay != spamDelay && this.spamDelay != -1)
            fireChangedEvent();
        this.spamDelay = spamDelay;
    }

    public void setHoldDelay(int holdDelay) {
        if (this.holdDelay != holdDelay && this.holdDelay != -1)
            fireChangedEvent();
        this.holdDelay = holdDelay;
    }

    public void setDebounceDelay(int debounceDelay) {
        if (this.debounceDelay != debounceDelay && this.debounceDelay != -1)
            fireChangedEvent();
        this.debounceDelay = debounceDelay;
    }

    public void setSleepDelay(int sleepDelay) {
        if (this.sleepDelay != sleepDelay && this.sleepDelay != -1)
            fireChangedEvent();
        this.sleepDelay = sleepDelay;
    }

    public void setLedBright(int ledBright) {
        if (this.ledBright != ledBright && this.ledBright != -1)
            fireChangedEvent();
        this.ledBright = ledBright;
    }

    /**
     * Sets a {@link Layer} to the {@link #layers} array.<br>
     *
     * @param idx   The idx between 0 and {@link #NUM_LAYERS}-1
     * @param layer The {@link Layer}-object to store
     */
    public void setLayer(int idx, Layer layer) {
        if (0 <= idx && idx < NUM_LAYERS) this.layers[idx] = layer;
    }

    public void valueHasChanged() {
        this.changed = true;
    }

    /**
     * Resets the changed-flag.
     */
    public void resetChanged() {
        this.changed = false;
    }
    // endregion

    /**
     * Fires a changed event to {@link #valueHasChanged()}.
     */
    private void fireChangedEvent() {
        valueHasChanged();
    }

}
