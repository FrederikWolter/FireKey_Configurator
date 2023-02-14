package com.firekey.configurator.arduino;

import com.fazecast.jSerialComm.SerialPort;
import com.firekey.configurator.FireKey;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ArduinoCLI {

    // region constants
    private static final String CLI_RESOURCES_PATH = "arduino" + File.separator;
    private static final String AVR_PATH = CLI_RESOURCES_PATH + "data" + File.separator + "packages" + File.separator + "arduino" + File.separator + "hardware" + File.separator + "avr" + File.separator + "1.8.6" + File.separator;       //TODO make version dynamic
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

    private static final String FIRE_KEY_BOARD_CORE = "arduino:avr";    // TODO really needed as const?
    // endregion

    // region attributes
    private final String dataPath;
    // endregion

    public ArduinoCLI(String dataPath) {
        this.dataPath = dataPath;
    }

    public void upload(String port) {
        // TODO
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
     * @throws Exception
     */
    public void init(TextArea textArea) throws Exception {
        // TODO use constants (+ separator?)
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
        executeArduinoCLI(LIB_INSTALL_CMD + KEYBOARD_LIB, textArea);
        executeArduinoCLI(LIB_INSTALL_CMD + USB_HOST_LIB, textArea);
        executeArduinoCLI(LIB_INSTALL_CMD + NEO_PIXEL_LIB, textArea);
        executeArduinoCLI(LIB_INSTALL_CMD + BUS_IO_LIB, textArea);
        executeArduinoCLI(LIB_INSTALL_CMD + GFX_LIB, textArea);
        executeArduinoCLI(LIB_INSTALL_CMD + U8G2_LIB, textArea);
        executeArduinoCLI(LIB_INSTALL_CMD + U8G2_LIB, textArea);
        // endregion

        // region install board
        executeArduinoCLI(CORE_INSTALL_CMD + FIRE_KEY_BOARD_CORE, textArea).onExit().thenAccept(process -> {
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

    private Process executeArduinoCLI(String command, TextArea textArea) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "cmd.exe", "/c", dataPath + CLI_RESOURCES_PATH + "arduino-cli.exe " + command + " --config-file \"" + dataPath + CLI_RESOURCES_PATH + "arduino-cli.yaml\"");
        // TODO cmd.exe etc. not needed?
        processBuilder.directory(new File(dataPath + CLI_RESOURCES_PATH));
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();
        // TODO use cool design pattern for chaining?
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;    // TODO why?
                        Platform.runLater(() -> {
                            textArea.appendText(">" + finalLine + "\n");
                            textArea.setScrollTop(Double.MAX_VALUE);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();

        return p;   // TODO/CHECK: Add "Consumer<Process> onExit" as lambda action on exit to the function?
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
    private void exportResource(String resourceName, String targetName) throws Exception {  // TODO generalize this?
        File exportFile = new File(dataPath + resourceName);

        if (exportFile.exists())
            return;

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
    }   // TODO cleanup installation / file moving procedure
}
