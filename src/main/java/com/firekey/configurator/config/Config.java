package com.firekey.configurator.config;

import javafx.scene.paint.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;

/**
 * Represents a whole FireKey config including its {@link Layer}s and the corresponding {@link Key}.
 */
public class Config {
    //TODO comments
    // TODO implement changed system incl. getter
    //TODO implement toFirmware & helpers

    // region constants
    public static final int NUM_LAYERS = 5;
    private static final String CONFIG_FILE_NAME = "firekey_config.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "firekey_config_default.json";
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
        this.setSpamDelay(configJson.getInt("spamDelay"));  // TODO extract all names to const?
        this.setHoldDelay(configJson.getInt("holdDelay"));
        this.setDebounceDelay(configJson.getInt("debounceDelay"));
        this.setSleepDelay(configJson.getInt("sleepDelay"));
        this.setLedBright(configJson.getInt("ledBright"));

        // get layers
        JSONArray layerJSONArray = configJson.getJSONArray("layers");
        for (int layerIdx = 0; layerIdx < NUM_LAYERS; layerIdx++) {
            // get current layer
            JSONObject layerJson = layerJSONArray.optJSONObject(layerIdx);
            if (layerJson != null) {
                // get layer values
                Layer layer = new Layer(layerJson.getString("name"));

                // get keys
                JSONArray keyJSONArray = layerJson.getJSONArray("keys");
                for (int keyIdx = 0; keyIdx < Layer.NUM_KEYS; keyIdx++) {
                    // get current key
                    JSONObject keyJson = keyJSONArray.optJSONObject(keyIdx);
                    if (keyJson != null) {
                        Key key = new Key(
                                keyJson.getString("name"),
                                keyJson.getEnum(KeyType.class, "type"),
                                keyJson.getString("function"),
                                Color.web(keyJson.getString("defaultColor"))
                        );
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
    public void saveConfig() throws IOException {
        try (FileWriter fw = new FileWriter(dataPath + CONFIG_FILE_NAME)) {
            fw.write(this.toJSON().toString(1));
        } catch (IOException e) {
            throw new IOException();    //TODO own exception?
        }
    }

    public void toFirmware() {
        //TODO
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
            if (l != null)
                layersJson.put(l.toJSON());
        }

        return new JSONObject()
                .put("spamDelay", this.spamDelay)
                .put("holdDelay", this.holdDelay)
                .put("debounceDelay", this.debounceDelay)
                .put("sleepDelay", this.sleepDelay)
                .put("ledBright", this.ledBright)
                .put("layers", layersJson);
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
        if (0 <= idx && idx < NUM_LAYERS)
            return this.layers[idx];
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
        if (0 <= idx && idx < NUM_LAYERS)
            this.layers[idx] = layer;
    }
    // endregion

}
