package com.firekey.configurator.arduino;

import com.fazecast.jSerialComm.SerialPort;
import com.firekey.configurator.FireKey;
import com.firekey.configurator.auxiliary.ICallBack;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class to execute ArduinoCLI commands in an OS-Shell
 */
public class ArduinoCLI {

    // region constants
    /**
     * Path to the firmware-files inside the resources. <br>
     * We have to use '/', because this is the separator inside a jar file.
     */
    private static final String FIRMWARE_RESOURCE_PATH = "firmware/";

    /**
     * Defines the path to the firmware-files in the {@link #dataPath}
     */
    public static final String FIRMWARE_DATA_PATH = "Firmware" + File.separator;

    /**
     * Defines the path to the arduino-cli resources in the {@link #dataPath}
     */
    private static final String CLI_RESOURCES_PATH = "Arduino" + File.separator;

    /**
     * Defines the path to the avr path where the 'boards.txt' is (in the {@link #dataPath})
     */
    private static final String AVR_PATH = CLI_RESOURCES_PATH + "data" + File.separator + "packages" + File.separator + "arduino" + File.separator + "hardware" + File.separator + "avr" + File.separator + "1.8.6" + File.separator;

    // region arduino-cli commands
    /**
     * The Lib command where we can install a lib
     */
    private static final String LIB_CMD = "lib";

    /**
     * The core (boards) command
     */
    private static final String CORE_CMD = "core";

    /**
     * Sub command to install a board or lib
     */
    private static final String INSTALL_CMD = "install";
    /**
     * Command to upload an ino-file
     */
    private static final String UPLOAD_CMD = "upload";

    /**
     * Command to compile an ino-file
     */
    private static final String COMPILE_CMD = "compile";
    // endregion

    // region libs
    /**
     * Name of the keyboard lib.
     */
    private static final String KEYBOARD_LIB = "Keyboard";

    /**
     * Name of the usb-host lib.
     */
    private static final String USB_HOST_LIB = "USBHost";

    /**
     * Name of the keyboard lib.
     */
    private static final String NEO_PIXEL_LIB = "\"Adafruit NeoPixel\"";

    /**
     * Name of the bus-io lib.
     */
    private static final String BUS_IO_LIB = "\"Adafruit BusIO\"";

    /**
     * Name of the adafruit-gfx-lib.
     */
    private static final String GFX_LIB = "\"Adafruit GFX Library\"";

    /**
     * Name of the display u8g2 lib.
     */
    private static final String U8G2_LIB = "\"U8g2\"";
    // endregion

    // region arduino core
    /**
     * The board version limited to a specific version, because we need to update inside this path the boards.txt (could change in future)
     */
    private static final String FIRE_KEY_BOARD_CORE = "arduino:avr@1.8.6";
    // endregion

    // endregion

    // region attributes
    /**
     * The resources directory for this app
     */
    private final String dataPath;
    /**
     * Flag that defines, if all resources for the firmware are installed.
     */
    private boolean isInstalled;

    /**
     * Flag that defines, if an upload process is running.
     */
    private boolean uploading;
    // endregion

    public ArduinoCLI(String dataPath) {
        this.dataPath = dataPath;
        this.isInstalled = false;
        this.uploading = false;
    }

    /**
     * Uploads the FireKey-Firmware to the selected board
     *
     * @param port       The selected COM-Port of the FireKey (e.G. COM5)
     * @param textArea   The reference to the text area where the log is written
     * @param onFinished Called, after the upload is finished
     * @param onError    Called, if an error occurs
     * @throws IOException If an I/O error occurs
     * @see #runArduinoCLI(TextArea, String...)
     */
    public void upload(String port, TextArea textArea, ICallBack onFinished, ICallBack onError) throws IOException {
        textArea.appendText(">Compiling Firmware...\n");
        this.uploading = true;
        this.runArduinoCLI(textArea, COMPILE_CMD, "-p", port, dataPath + FIRMWARE_DATA_PATH).onExit().thenAccept(process -> {
            textArea.appendText(">Done\n");
            textArea.appendText(">Uploading Firmware...\n");
            try {
                this.runArduinoCLI(textArea, UPLOAD_CMD, "-p", port, dataPath + FIRMWARE_DATA_PATH).onExit().thenAccept(process1 -> {
                    textArea.appendText(">Done\n");
                    this.uploading = false;
                    onFinished.invoke();
                });
            } catch (IOException e) {
                this.uploading = false;
                onError.invoke();
                throw new RuntimeException(e);
            }
        });
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
     * @return This object
     * @throws Exception If an I/O error occurs ({@link ProcessBuilder#start()}
     */
    public ArduinoCLI init(TextArea textArea) throws Exception {
        // region copy required files
        exportResource("arduino-cli.exe", CLI_RESOURCES_PATH + "arduino-cli.exe", false);
        exportResource("arduino-cli.yaml", CLI_RESOURCES_PATH + "arduino-cli.yaml", false);
        exportResource(FIRMWARE_RESOURCE_PATH + "Config.h", FIRMWARE_DATA_PATH + "Config.h", false);
        exportResource(FIRMWARE_RESOURCE_PATH + "Config.h", FIRMWARE_RESOURCE_PATH + "Config_default.h", true);
        exportResource(FIRMWARE_RESOURCE_PATH + "Debug.h", FIRMWARE_DATA_PATH + "Debug.h", true);
        exportResource(FIRMWARE_RESOURCE_PATH + "Firmware.ino", FIRMWARE_DATA_PATH + "Firmware.ino", true);
        exportResource(FIRMWARE_RESOURCE_PATH + "Key.h", FIRMWARE_DATA_PATH + "Key.h", true);
        // endregion

        // region install libs
        runArduinoCLI(textArea, LIB_CMD, INSTALL_CMD, KEYBOARD_LIB);
        runArduinoCLI(textArea, LIB_CMD, INSTALL_CMD, USB_HOST_LIB);
        runArduinoCLI(textArea, LIB_CMD, INSTALL_CMD, NEO_PIXEL_LIB);
        runArduinoCLI(textArea, LIB_CMD, INSTALL_CMD, BUS_IO_LIB);
        runArduinoCLI(textArea, LIB_CMD, INSTALL_CMD, GFX_LIB);
        runArduinoCLI(textArea, LIB_CMD, INSTALL_CMD, U8G2_LIB);
        // endregion

        // region install board
        runArduinoCLI(textArea, CORE_CMD, INSTALL_CMD, FIRE_KEY_BOARD_CORE).onExit().thenAccept(process -> {
            try {
                // copy boards.txt with FireKey corresponding data
                exportResource("boards.txt", AVR_PATH + "boards.txt", true);
                this.isInstalled = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // region install board
        return this;
    }

    /**
     * Executes an arduino-cli command inside a process
     *
     * @param textArea The reference to the text area where the log is written
     * @param commands The array of the command snippets which will be executed
     * @return The started {@link Process}
     * @throws IOException If an I/O error occurs ({@link ProcessBuilder#start()}
     * @see #buildArduinoCLIProcess(TextArea, List)
     */
    private Process runArduinoCLI(TextArea textArea, String... commands) throws IOException {
        List<String> runCommands = new ArrayList<>();
        runCommands.add(dataPath + CLI_RESOURCES_PATH + "arduino-cli.exe");
        Collections.addAll(runCommands, commands);
        return buildArduinoCLIProcess(textArea, runCommands);
    }

    /**
     * Creates the arduino-cmd command and executes it
     *
     * @param textArea The reference to the text area where the log is written
     * @param commands The snippets of the command, which will be executed
     * @throws IOException If an I/O error occurs ({@link ProcessBuilder#start()}
     * @return The started {@link Process}
     */
    private Process buildArduinoCLIProcess(TextArea textArea, List<String> commands) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.environment().put("config-file", dataPath + CLI_RESOURCES_PATH + "arduino-cli.yaml");
        processBuilder.directory(new File(dataPath + CLI_RESOURCES_PATH));
        processBuilder.redirectErrorStream(true);
        Process p = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
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

        return p;
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName TThe name of the resource to copy
     * @param targetName   The target output file
     * @param override     True, if a file should be forced to be overwritten
     * @throws Exception If the target file cant be found.
     * @see #dataPath
     */
    private void exportResource(String resourceName, String targetName, boolean override) throws Exception {  // TODO generalize this?
        File exportFile = new File(dataPath + targetName);

        if (exportFile.exists() && !override)
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

    /**
     * @return true, if the ArduinoCLI is ready to use
     */
    public boolean isInstalled() {
        return isInstalled;
    }

    /**
     * @return true, if an upload process is running
     */
    public boolean isUploading() {
        return uploading;
    }
}
