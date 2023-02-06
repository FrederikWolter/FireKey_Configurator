package com.firekey.configurator;

import com.firekey.configurator.gui.HelloApplication;
import javafx.application.Application;
import com.fazecast.jSerialComm.SerialPort;

/**
 * Main entrypoint in this app.
 */
public class FireKey {

    public static void main(String[] args) {
        SerialPort [] ports = SerialPort.getCommPorts();

        //TODO HelloApplication Controller remove / rename -> FireKey Controller
        Application.launch(HelloApplication.class, args);
    }
}
