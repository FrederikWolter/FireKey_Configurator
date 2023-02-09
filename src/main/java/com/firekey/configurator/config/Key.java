package com.firekey.configurator.config;

import javafx.scene.paint.Color;
import org.json.JSONObject;

/**
 * Represents a single key on a {@link Layer} of the macro-keyboard FireKey.
 *
 * @see Layer
 */
public class Key {
    //region attributes
    /**
     * The display name of this {@link Key}-object
     */
    private String name;

    /**
     * The {@link KeyType} of this {@link Key}-object
     */
    private KeyType type;

    /**
     * The function of this {@link Key}-object, which is executed on the keyboard if the corresponding key is pressed (depending on layer)
     *
     * @see Layer
     */
    private String function;

    /**
     * The default color of this {@link Key}-object (depending on layer)
     *
     * @see Layer
     */
    private Color defaultColor;
    //endregion

    /**
     * Default constructor of a {@link Key}-object for a corresponding {@link Layer}.
     *
     * @param name         The display name of this {@link Key}
     * @param type         The {@link KeyType} of this {@link Key}
     * @param function     The function, this {@link Key} is executing on press. Only set, if {@link #type} is not {@link KeyType#Layer}!
     * @param defaultColor The default color for this {@link Key}
     */
    public Key(String name, KeyType type, String function, Color defaultColor) {
        this.name = name;
        this.type = type;
        if (type != KeyType.Layer)
            this.function = function;
        this.defaultColor = defaultColor;
    }

    // region getter

    /**
     * @return The {@link #name} of this {@link Key}-object
     */
    public String getName() {
        return name;
    }

    /**
     * @return The {@link #type} of this {@link Key}-object
     * @see KeyType
     */
    public KeyType getType() {
        return type;
    }

    /**
     * @return The {@link #function} of this {@link Key}-object
     */
    public String getFunction() {
        return function;
    }

    /**
     * @return The {@link #defaultColor} of this {@link Key}-object
     */
    public Color getDefaultColor() {
        return defaultColor;
    }

    /**
     * Converts a {@link Key}-object into a {@link JSONObject} to save it in a JSON-file.
     *
     * @return The corresponding {@link JSONObject}
     */
    public JSONObject toJSON() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("name", this.getName());
        jsonObj.put("type", this.getType());
        jsonObj.put("function", this.getFunction());
        jsonObj.put("defaultColor", this.getDefaultColor());
        return jsonObj;
    }

    // endregion

    // region setter

    /**
     * Sets the {@link #name} of this {@link Key}-object.
     * Only if the {@link #type} is not {@link KeyType#Layer}.
     *
     * @param name The new name of this {@link Key}-object
     */
    public void setName(String name) {
        if (this.getType() != KeyType.Layer)
            this.name = name;
    }

    /**
     * Sets the {@link #type} of this {@link Key}-object
     *
     * @param type The new typee of this {@link Key}-object
     */
    public void setType(KeyType type) {
        this.type = type;
    }

    /**
     * Sets the {@link #function} of this {@link Key}-object.
     * Only if the {@link #type} is not {@link KeyType#Layer}.
     *
     * @param function The new action-function of this {@link Key}-object
     */
    public void setFunction(String function) {
        if (this.getType() != KeyType.Layer)
            this.function = function;
    }

    /**
     * Sets the {@link #defaultColor} of this {@link Key}-object
     *
     * @param defaultColor The new default layer color of this {@link Key}-object
     */
    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    // endregion
}
