package com.firekey.configurator.config;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Represents a single layer of the macro-keyboard FireKey.
 *
 * @see Key
 * @see Config
 */
public class Layer {

    // region attributes
    /**
     * Number of keys per {@link Layer} the macro-keyboard FireKey has.
     */
    private static final int NUM_KEYS = 15;

    /**
     * The display name of the layer
     */
    private String name;

    private final Key[] keys;
    //  endregion

    /**
     * Default constructor for a {@link Layer} of the macro-keyboard FireKey.
     *
     * @param name The display name of this {@link Layer}
     */
    public Layer(String name) {
        this.name = name;
        this.keys = new Key[NUM_KEYS];
    }

    // region getter

    public String getName() {
        return name;
    }

    /**
     * Gets the specific {@link Key} object.
     *
     * @param idx The index of the {@link Key}, which should be returned.
     * @return The {@link Key}-object or null, if the idx is out of bounce or the {@link Key}-object does not exist.
     */
    public Key getKey(int idx) {
        if (0 <= idx && idx < NUM_KEYS)
            return this.keys[idx];
        return null;
    }

    /**
     * Converts this {@link Layer}-object into a {@link JSONObject} to save it in a JSON-file.
     * Containing all {@link Key}-objects in a {@link JSONArray}.
     *
     * @return The corresponding {@link JSONObject}
     * @see Key
     * @see JSONObject
     * @see JSONArray
     */
    public JSONObject toJSON() {
        JSONObject layerJSONObj = new JSONObject();
        JSONArray layerKeysJSONArray = new JSONArray();
        layerJSONObj.put("name", this.name);

        for (int idx = 0; idx < NUM_KEYS; idx++) {
            if (this.keys[idx] != null)
                layerKeysJSONArray.put(idx, this.keys[idx].toJSON());
            else
                layerKeysJSONArray.put(idx, "null");
        }
        layerJSONObj.put("keys", layerKeysJSONArray);

        return layerJSONObj;
    }

    // endregion

    // region setter

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds/Overrides a {@link Key} to the {@link #keys} array
     * Arrays index starts at 0 so the first key needs to have array index 0. (Key1 -> idx 0)
     *
     * @param idx The idx between 0 and {@link #NUM_KEYS}. ({@link #NUM_KEYS} excluding)
     * @param key The {@link Key}-Object to store.
     */
    public void addKey(int idx, Key key) {
        if (0 <= idx && idx < NUM_KEYS)
            this.keys[idx] = key;
    }

    // endregion
}
