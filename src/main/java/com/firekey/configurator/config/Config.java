package com.firekey.configurator.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Represents a whole FireKey config including its {@link Layer}s and the corresponding {@link Key}
 *
 * @see Layer
 * @see Key
 */
public class Config {
    //TODO comments
    //TODO implement loadConfig, toFirmware & helpers

    // region attributes

    private static final int NUM_LAYERS = 5;
    private static final String CONFIG_FILE_NAME = "firekey_config.json";
    private static final String DEFAULT_CONFIG_FILE_NAME = "firekey_default_config.json";

    private int spamDelay;

    private int holdDelay;

    private int debounceDelay;

    private int sleepDelay;

    private int ledBright;

    private final Layer[] layers;

    private final String jarFolder;

    // endregion

    public Config(int spamDelay, int holdDelay, int debounceDelay, int sleepDelay, int ledBright) throws URISyntaxException {
        this.spamDelay = spamDelay;
        this.holdDelay = holdDelay;
        this.debounceDelay = debounceDelay;
        this.sleepDelay = sleepDelay;
        this.ledBright = ledBright;
        this.layers = new Layer[NUM_LAYERS];
        jarFolder = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace("\\", File.separator);
    }

    public void loadConfig() {
        //TODO
    }

    /**
     * Saves this {@link Config} to the {@link #CONFIG_FILE_NAME}-file next to the jar-file.
     *
     * @throws IOException
     */
    public void saveConfig() throws IOException {
        try (FileWriter fw = new FileWriter(jarFolder + File.separator + CONFIG_FILE_NAME)) {
            fw.write(this.toJSON().toString(1));
        } catch (IOException e) {
            throw new IOException();    //TODO own exception?
        }
    }

    public void toFirmware() {
        //TODO
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

    /**
     * Converts this {@link Config}-object into a {@link JSONObject} to save it in a JSON-file.
     * Containing all {@link Layer}-objects in a {@link JSONArray}.
     *
     * @return The corresponding {@link JSONObject}
     * @see Layer#toJSON()
     * @see JSONObject
     * @see JSONArray
     */
    public JSONObject toJSON() {
        JSONObject configJSONObj = new JSONObject();
        JSONArray configLayerJSONArray = new JSONArray();
        configJSONObj.put("spamDelay", this.spamDelay);
        configJSONObj.put("holdDelay", this.holdDelay);
        configJSONObj.put("debounceDelay", this.debounceDelay);
        configJSONObj.put("sleepDelay", this.sleepDelay);
        configJSONObj.put("ledBright", this.ledBright);

        for (int idx = 0; idx < NUM_LAYERS; idx++) {
            if (this.layers[idx] != null)
                configLayerJSONArray.put(idx, this.layers[idx].toJSON());
            else
                configLayerJSONArray.put(idx, "null");
        }
        configJSONObj.put("layers", configLayerJSONArray);

        return configJSONObj;
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
     * Adds/Overrides a {@link Layer} to the {@link #layers} array
     * Arrays index starts at 0 so the first {@link Layer} needs to have array index 0. (Layer1 -> idx 0)
     *
     * @param idx   The idx between 0 and {@link #NUM_LAYERS}. ({@link #NUM_LAYERS} excluding)
     * @param layer The {@link Layer}-Object to store.
     */
    public void addLayer(int idx, Layer layer) {
        if (0 <= idx && idx < NUM_LAYERS)
            this.layers[idx] = layer;
    }


    // endregion

}
