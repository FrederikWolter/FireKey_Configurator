package com.firekey.configurator;

import com.firekey.configurator.gui.MainApplication;
import javafx.application.Application;

/**
 * Main entrypoint in this app.
 */
public class FireKey {

    public static final String VERSION = "V0.6.2";

    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }

}
