package com.firekey.configurator.arduino;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import com.fazecast.jSerialComm.SerialPort;
import com.firekey.configurator.FireKey;

public class ArduinoCLI {

    // region attributes
    private static final String CLI_RESOURCES_PATH = "arduino" + File.separator;

    //TODO make version dynamic
    private static final String AVR_PATH = CLI_RESOURCES_PATH + "data" + File.separator + "packages" + File.separator + "arduino" + File.separator + "hardware" + File.separator +"avr" + File.separator + "1.8.6" + File.separator;

    private static final String LIB_INSTALL_CMD = "lib install ";
    private static final String CORE_INSTALL_CMD = "core install ";

    // region libs
    private static final String KEYBOARD_LIB = "Keyboard";
    private static final String USB_HOST_LIB = "USBHost";
    private static final String NEO_PIXEL_LIB = "\"Adafruit NeoPixel\"";
    private static final String BUS_IO_LIB = "\"Adafruit BusIO\"";
    private static final String GFX_LIB = "\"Adafruit GFX Library\"";
    private static final String U8G2_LIB = "\"U8g2\"";
    // endregion

    private static final String FIRE_KEY_BOARD_CORE = "arduino:avr";

    private final String dataPath;

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
        for (SerialPort port : ports) {
            String deviceName = port.getPortDescription() + " (" + port.getSystemPortName() + ")";
            deviceNames.add(deviceName);
        }
        return deviceNames;
    }

    /**
     * Install all required libs and boards
     *
     * @throws IOException
     * @see #executeArduinoCLI(String)
     */
    public void init() throws Exception {
        // region copy required files
        exportResource("arduino-cli.exe", CLI_RESOURCES_PATH + "arduino-cli.exe");
        exportResource("arduino-cli.yaml", CLI_RESOURCES_PATH + "arduino-cli.yaml");
        exportResource("firmware/Config.h");
        exportResource("firmware/Config.h", "firmware/Config_default.h");
        exportResource("firmware/Debug.h");
        exportResource("firmware/Firmware.ino");
        exportResource("firmware/Key.h");
        // endregion


        // region install libs

        executeArduinoCLI(LIB_INSTALL_CMD + KEYBOARD_LIB);

        executeArduinoCLI(LIB_INSTALL_CMD + USB_HOST_LIB);

        executeArduinoCLI(LIB_INSTALL_CMD + NEO_PIXEL_LIB);

        executeArduinoCLI(LIB_INSTALL_CMD + BUS_IO_LIB);

        executeArduinoCLI(LIB_INSTALL_CMD + GFX_LIB);

        executeArduinoCLI(LIB_INSTALL_CMD + U8G2_LIB);

        executeArduinoCLI(LIB_INSTALL_CMD + U8G2_LIB);
        // endregion

        // region install board
        executeArduinoCLI(CORE_INSTALL_CMD + FIRE_KEY_BOARD_CORE).onExit().thenAccept(process -> {
            try {
                // copy boards.txt with FireKey corresponding data
                // TODO make dynamic (path could change in future)
                exportResource("boards.txt", AVR_PATH + "boards.txt");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // region install board
    }

    private Process executeArduinoCLI(String command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", dataPath + CLI_RESOURCES_PATH + "arduino-cli.exe " + command + " --config-file \"" + dataPath + CLI_RESOURCES_PATH + "arduino-cli.yaml\"");
        processBuilder.directory(new File(dataPath + CLI_RESOURCES_PATH));
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        // TODO create install status bar
        // TODO remove sync prints
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
        return p;
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName The name of the resource to copy
     * @throws Exception If the target file cant be found.
     * @see #exportResource(String, String)
     */
    private void exportResource(String resourceName) throws Exception {
        exportResource(resourceName, resourceName);
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName TThe name of the resource to copy
     * @param targetName   The target output file
     * @throws Exception If the target file cant be found.
     * @see #dataPath
     */
    private void exportResource(String resourceName, String targetName) throws Exception {
        File exportFile = new File(dataPath + resourceName);
        if (exportFile.exists()) {
            return;
        }
        // We use FireKey here to use the FireKey-level as resource root
        try (InputStream stream = FireKey.class.getResourceAsStream(resourceName)) {
            if (stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file."); // TODO custom exception?
            }
            // create needed folders
            Path newFilePath = Path.of(dataPath + targetName);
            int fileNameSplitPos = newFilePath.toString().lastIndexOf(File.separator);
            Path folders = Path.of(newFilePath.toString().substring(0, fileNameSplitPos + 1));
            Files.createDirectories(folders);

            // copy file
            Files.copy(stream, newFilePath, StandardCopyOption.REPLACE_EXISTING);
        }

    }

}
