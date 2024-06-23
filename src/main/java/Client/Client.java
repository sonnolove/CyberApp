package Client;

import Controller.FXMLDocumentController;
import Controller.dashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Client extends Application {
    private SocketHandle socketHandle;
    private Stage primaryStage;

    private static Client instance;

    public Client() {
        try {
            socketHandle = new SocketHandle("localhost", 12345); // Initialize SocketHandle with server address and port
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception gracefully (e.g., show error dialog, exit application)
        }
    }

    // JavaFX application start method
    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();

        FXMLDocumentController controller = loader.getController();
        controller.setClient(this);
    }

    // Method to load and display Login.fxml
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

            // Pass the SocketHandle instance to the controller of Login.fxml
            FXMLDocumentController controller = loader.getController();
            if (controller != null) {
                controller.setSocketHandle(socketHandle);
            } else {
                System.err.println("FXMLDocumentController is null. Check your FXML file and controller assignment.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            // Handle exception gracefully (e.g., show error dialog)
        }
    }

    // Method to load and display Client.fxml
    public void showClientMainView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Client.fxml"));
            Parent root = loader.load();
            dashboardController controller = loader.getController();
            if (controller != null) {
                controller.setSocketHandle(socketHandle); // Pass SocketHandle to DashboardController
            } else {
                System.err.println("DashboardController is null. Check your FXML file and controller assignment.");
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    // Main method to launch JavaFX application
    public static void main(String[] args) {
        launch(args);
    }

    // Getter method for SocketHandle
    public SocketHandle getSocketHandle() {
        return socketHandle;
    }
}
