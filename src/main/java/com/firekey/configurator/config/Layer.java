package com.firekey.configurator.config;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Represents a single layer of the macro-keyboard FireKey managing all its {@link Key}s.
 */
public class Layer {
    // region constants
    /**
     * Number of keys per {@link Layer} the macro-keyboard FireKey has.
     */
    public static final int NUM_KEYS = 15;
    public static final String NAME = "name";
    public static final String KEYS = "keys";
    // endregion

    // region attributes
    /**
     * The display name of the {@link Layer}
     */
    private String name;
    /**
     * The list of {@link Key}s in this {@link Layer}
     * The last keys need to be the navigation keys
     */
    private final Key[] keys;
    /**
     * The {@link Config}-object this layer is in.
     */
    private final Config config;

    //  endregion

    /**
     * Default constructor for a {@link Layer}.
     *
     * @param name The display name of this {@link Layer}
     */
    public Layer(String name, Config config) {
        this.name = name;
        this.keys = new Key[NUM_KEYS];
        this.config = config;
    }

    /**
     * Converts this {@link Layer} into a {@link JSONObject} to save it to the config file.<br>
     * Containing all {@link Key}s in a {@link JSONArray}.
     *
     * @return The corresponding {@link JSONObject}
     * @see Key#toJSON()
     */
    public JSONObject toJSON() {
        // convert all Keys
        JSONArray keysJson = new JSONArray();
        for (Key k : this.keys) {
            if (k != null)
                keysJson.put(k.toJSON());
        }

        return new JSONObject()
                .put(NAME, this.name)
                .put(KEYS, keysJson);
    }

    // region getter

    /**
     * @return The {@link #name} of the {@link Layer}
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the specific {@link Key}.
     *
     * @param idx The index of the {@link Key}
     * @return The {@link Key} or null, if the idx is out of bounce or the {@link Key} does not exist.
     */
    public Key getKey(int idx) {
        if (0 <= idx && idx < NUM_KEYS)
            return this.keys[idx];
        return null;
    }
    // endregion

    // region setter

    /**
     * Sets the {@link #name} of this {@link Layer}.
     *
     * @param name The new {@link #name} of this {@link Layer}
     */
    public void setName(String name) {
        if(!Objects.equals(this.name, name))
            fireChangedEvent();
        this.name = name;
    }

    /**
     * Sets a {@link Key} to the {@link #keys} array.<br>
     *
     * @param idx The idx between 0 and {@link #NUM_KEYS}-1
     * @param key The {@link Key} to store
     */
    public void setKey(int idx, Key key) {
        if (0 <= idx && idx < NUM_KEYS)
            this.keys[idx] = key;
    }
    // endregion

    private void fireChangedEvent(){
        config.valueHasChanged();
    }
}
