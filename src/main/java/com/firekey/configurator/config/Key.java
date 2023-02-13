package com.firekey.configurator.config;

import javafx.scene.paint.Color;
import org.json.JSONObject;

/**
 * Represents a single key on a specific {@link Layer} of the macro-keyboard FireKey.
 */
public class Key {
    // region attributes
    /**
     * The display name of this {@link Key}
     */
    private String name;
    /**
     * The {@link KeyType} of this {@link Key}<br>
     * This can be used to identify how to interpret other attributes like {@link #function}.
     */
    private KeyType type;
    /**
     * The function of this {@link Key} executed on the keyboard if the key is pressed with the corresponding {@link Layer} selected .
     */
    private String function;
    /**
     * The default color of this {@link Key} with the corresponding {@link Layer} selected.
     */
    private Color defaultColor;
    // endregion

    /**
     * Default constructor of a {@link Key} for a corresponding {@link Layer}.
     *
     * @param name         The display name of this {@link Key}
     * @param type         The {@link KeyType} of this {@link Key}
     * @param function     The function, this {@link Key} is executing on press.
     * @param defaultColor The default color for this {@link Key}
     */
    public Key(String name, KeyType type, String function, Color defaultColor) {
        this.name = name;
        this.type = type;
        this.function = function;
        this.defaultColor = defaultColor;
    }

    /**
     * Converts a {@link Key} into a {@link JSONObject} to save it to the config file.
     *
     * @return converted {@link JSONObject}
     */
    public JSONObject toJSON() {
        return new JSONObject()
                .put("name", this.getName())
                .put("type", this.getType())
                .put("function", this.getFunction())
                .put("defaultColor", this.getDefaultColor());
    }

    // region getter
    /**
     * @return The {@link #name} of this {@link Key}
     */
    public String getName() {
        return name;
    }

    /**
     * @return The {@link #type} of this {@link Key}
     */
    public KeyType getType() {
        return type;
    }

    /**
     * @return The {@link #function} of this {@link Key}
     */
    public String getFunction() {
        return function;
    }

    /**
     * @return The {@link #defaultColor} of this {@link Key}
     */
    public Color getDefaultColor() {
        return defaultColor;
    }
    // endregion

    // region setter
    /**
     * Sets the {@link #name} of this {@link Key}.<br>
     * Not used if {@link #type} is {@link KeyType#NAV_UP}, {@link KeyType#NAV_DOWN}, {@link KeyType#NAV_HOME}.
     *
     * @param name The new name of this {@link Key}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the {@link #type} of this {@link Key}.
     *
     * @param type The new type of this {@link Key}
     */
    public void setType(KeyType type) {
        this.type = type;
    }

    /**
     * Sets the {@link #function} of this {@link Key}.<br>
     * Not used if the {@link #type} is {@link KeyType#NAV_UP}, {@link KeyType#NAV_DOWN}, {@link KeyType#NAV_HOME}.
     *
     * @param function The new action-function of this {@link Key}
     */
    public void setFunction(String function) {
        this.function = function;
    }

    /**
     * Sets the {@link #defaultColor} of this {@link Key}.
     *
     * @param defaultColor The new default layer color of this {@link Key}
     */
    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }
    // endregion

}
