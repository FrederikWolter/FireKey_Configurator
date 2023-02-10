package com.firekey.configurator.arduino;

import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;

public class ArduinoCLI {

    // region attributes

    private String dataPath;

    // endregion

    public ArduinoCLI(String dataPath) {
        this.dataPath = dataPath;
    }

    public void upload(String port) {

    }

    /**
     * Get formatted device names + com-ports. e.g. "FireKey (COM5)"
     *
     * @return A {@link ArrayList} including all available com-port devices as the defined formatted string.
     */
    public List<String> getPorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<String> deviceNames = new ArrayList<>();
        if (ports.length > 0) {
            for (int i = 0; i < ports.length; i++) {
                String deviceName = ports[i].getPortDescription() + " (" + ports[i].getSystemPortName() + ")";
                deviceNames.add(deviceName);
            }
        }
        return deviceNames;
    }

    public void init() {

    }
}
