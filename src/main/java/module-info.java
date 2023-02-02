module com.firekey.configurator {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.firekey.configurator;
    exports com.firekey.configurator.gui;
    opens com.firekey.configurator to javafx.fxml;
    opens com.firekey.configurator.gui to javafx.fxml;
}