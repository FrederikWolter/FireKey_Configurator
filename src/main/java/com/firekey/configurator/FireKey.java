package com.firekey.configurator;

import com.firekey.configurator.gui.MainApplication;
import javafx.application.Application;

/**
 * Main entrypoint in this app.
 */
public class FireKey {

    public static final String VERSION = "V0.7.1";

    public static void main(String[] args) {
        Application.launch(MainApplication.class, args);
    }

}
