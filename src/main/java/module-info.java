module org.example.flower {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires jbcrypt;
    requires mysql.connector.java;
    requires spring.security.crypto;


    exports Controller;
    opens Controller to javafx.fxml;
    exports Model;
    opens Model to javafx.fxml;
    exports DAO;
    opens DAO to javafx.fxml;
    exports Server;
    opens Server to javafx.fxml;
    exports Client;
    opens Client to javafx.fxml;
    exports View;
    opens View to javafx.fxml;
}