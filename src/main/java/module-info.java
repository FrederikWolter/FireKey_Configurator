module com.firekey.configurator {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.firekey.configurator to javafx.fxml;
    exports com.firekey.configurator;
}