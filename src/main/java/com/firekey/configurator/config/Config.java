package com.firekey.configurator.config;

import com.firekey.configurator.arduino.ArduinoCLI;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a whole FireKey config including its {@link Layer}s and the corresponding {@link Key}.
 */
public class Config {
    // TODO comments
    // TODO implement changed system incl. getter

    // region constants
    public static final int NUM_LAYERS = 5;
    private static final String CONFIG_FILE_NAME = "firekey_config.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "firekey_config_default.json";
    public static final String HOLD_DELAY = "holdDelay";
    public static final String DEBOUNCE_DELAY = "debounceDelay";
    public static final String SLEEP_DELAY = "sleepDelay";
    public static final String LED_BRIGHT = "ledBright";
    public static final String SPAM_DELAY = "spamDelay";
    public static final String LAYERS = "layers";
    // endregion

    // region attributes
    private int spamDelay;
    private int holdDelay;
    private int debounceDelay;
    private int sleepDelay;
    private int ledBright;
    private final Layer[] layers;
    private final String dataPath;
    // endregion

    public Config(String dataPath) {
        this.layers = new Layer[NUM_LAYERS];
        this.dataPath = dataPath;
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

    public Config load() throws IOException {
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
                Layer layer = new Layer(layerJson.getString(Layer.NAME));

                // get keys
                JSONArray keyJSONArray = layerJson.getJSONArray(Layer.KEYS);
                for (int keyIdx = 0; keyIdx < Layer.NUM_KEYS; keyIdx++) {
                    // get current key
                    JSONObject keyJson = keyJSONArray.optJSONObject(keyIdx);
                    if (keyJson != null) {
                        Key key = new Key(keyJson.getString(Key.NAME), keyJson.getEnum(KeyType.class, Key.TYPE), keyJson.getString(Key.FUNCTION), Color.web(keyJson.getString(Key.DEFAULT_COLOR)));
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
     * @throws IOException
     */
    public void save() throws IOException {
        try (FileWriter fw = new FileWriter(dataPath + CONFIG_FILE_NAME)) {
            fw.write(this.toJSON().toString(1));
        } catch (IOException e) {
            throw new IOException();    //TODO own exception?
        }
    }

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
     * Build the config replacement map.
     * E.g. 'SPAM_DELAY 15' : 'SPAM_DELAY 50'
     *
     * @return
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
                definitions.put("Layer" + layerIdx, layer.getName());
                addKeyDefinitions(definitions, layerIdx, layer);
            }
        }
    }

    /**
     * Add the key information to the definitions map
     *
     * @param definitions The definitions-map
     * @param layerIdx The current layer index
     * @param layer The current layer
     */
    private void addKeyDefinitions(Map<String, String> definitions, int layerIdx, Layer layer) {
        for (int keyIdx = 0; keyIdx < Layer.NUM_KEYS; keyIdx++) {
            Key key = layer.getKey(keyIdx);
            if (key != null) {
                // add key function name
                if (key.getType() != KeyType.NAV_DOWN && key.getType() != KeyType.NAV_UP && key.getType() != KeyType.NAV_HOME)  // we can't change the navigation texts
                    definitions.put("L" + layerIdx + "K" + keyIdx, key.getName());

                // add default color
                int red = (int) (key.getDefaultColor().getRed() * 255);
                int green = (int) (key.getDefaultColor().getGreen() * 255);
                int blue = (int) (key.getDefaultColor().getBlue() * 255);
                definitions.put("{ 0, " + layerIdx + ", " + keyIdx + " }", "{ " + red + ", " + green + ", " + blue + " }");

                // add function
                if (key.getType() != KeyType.NAV_DOWN && key.getType() != KeyType.NAV_UP && key.getType() != KeyType.NAV_HOME) // we can't change the navigation functions
                    definitions.put("//K" + (keyIdx + 1) + "L" + layerIdx, key.getFunction());
            }
        }
    }

    /**
     * TODO
     * <a href='https://stackoverflow.com/questions/1326682/java-replacing-multiple-different-substring-in-a-string-at-once-or-in-the-most/40836618#40836618'> Resource </a>
     *
     * @param text
     * @param definitions
     * @return
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
    // endregion

    // region setter
    public void setSpamDelay(int spamDelay) {
        this.spamDelay = spamDelay;
    }

    public void setHoldDelay(int holdDelay) {
        this.holdDelay = holdDelay;
    }

    public void setDebounceDelay(int debounceDelay) {
        this.debounceDelay = debounceDelay;
    }

    public void setSleepDelay(int sleepDelay) {
        this.sleepDelay = sleepDelay;
    }

    public void setLedBright(int ledBright) {
        this.ledBright = ledBright;
    }

    /**
     * Sets a {@link Layer} to the {@link #layers} array.<br>
     *
     * @param idx   The idx between 0 and {@link #NUM_LAYERS}-1
     * @param layer The {@link Layer}-Object to store
     */
    public void setLayer(int idx, Layer layer) {
        if (0 <= idx && idx < NUM_LAYERS) this.layers[idx] = layer;
    }
    // endregion

}
