package com.firekey.configurator.config;

import javafx.scene.paint.Color;
import org.json.JSONObject;

public class Key {
    //region variables
    private String name;
    private KeyType type;
    private String function;
    private Color defaultColor;
    //endregion

    public Key(String name, KeyType type, String function, Color defaultColor) {
        this.name = name;
        this.type = type;
        this.function = function;
        this.defaultColor = defaultColor;
    }

    // region getter
    public String getName() {
        return name;
    }

    public KeyType getType() {
        return type;
    }

    public String getFunction() {
        return function;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }
    // endregion

    // region setter
    public void setName(String name) {
        this.name = name;
    }

    public void setType(KeyType type) {
        this.type = type;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public void setDefaultColor(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public JSONObject toJson(){
        //TODO
        return null;
    }
    // endregion
}
