package Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import Client.Client;
import Client.SocketHandle;
import DAO.database;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class FXMLDocumentController implements Initializable {
    @FXML
    private Button CreateBtn;

    @FXML
    private Button registerBtn;

    @FXML
    private Button SigninBtn;

    @FXML
    private AnchorPane signup_form;

    @FXML
    private AnchorPane main_form;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginBtn;

    @FXML
    private Button close;

    @FXML
    private AnchorPane signin_form;

    @FXML
    private PasswordField password1;

    @FXML
    private TextField username1;

    //    DATABASE TOOLS
    private SocketHandle socketHandle;
    private Statement statement;
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private Client client;


    public void setClient(Client client) {
        this.client = client;
    }
    public FXMLDocumentController() throws IOException {
        // Khởi tạo SocketHandle
        socketHandle = new SocketHandle("localhost", 12345); // Thay thế bằng giá trị thực tế
    }

    public void switchForm(ActionEvent event) {

        if (event.getSource() == registerBtn) {
            signup_form.setVisible(true);
            signin_form.setVisible(false);


        } else if (event.getSource() == SigninBtn) {
            signup_form.setVisible(false);
            signin_form.setVisible(true);

        }
    }

    public void Add() {
        String username = username1.getText();
        String password = password1.getText();

        // Kiểm tra dữ liệu nhập vào
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Please fill all blank fields", null);
            return;
        }

        try {
            if (socketHandle == null) {
                throw new IOException("SocketHandle is not initialized.");
            }

            String registrationMessage = "REGISTER:" + username + ":" + password; // Định dạng chuẩn
            socketHandle.sendMessage(registrationMessage);

            // Nhận phản hồi từ server
            String response = socketHandle.receiveMessage();
            System.out.println("Server response: " + response);

            if (response != null && response.equals("REGISTER_SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Added!", null);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Registration Failed", null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Unable to connect to server", "Please try again later.");
        }
    }


    public void login(){
        String Username = username.getText();
        String Password = password.getText();



        // Kiểm tra dữ liệu nhập vào
        if (Username.isEmpty() || Password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password", "Please check your credentials and try again.");
            return;
        }

        try {
            if (socketHandle == null) {
                throw new IOException("SocketHandle is not initialized.");
            }


            String loginMessage = "LOGIN:" + Username + ":" + Password; // Định dạng chuẩn
            socketHandle.sendMessage(loginMessage);


            String response = socketHandle.receiveMessage();
            System.out.println("Server response: " + response);


            if (response != null && response.equals("LOGIN_SUCCESS")) {
                client.showClientMainView();
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password", "Please check your credentials and try again.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Unable to connect to server", "Please try again later.");
        }
    }

    public void close(){
        System.exit(0);
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        client = new Client();
    }
    private void showAlert(Alert.AlertType alertType, String title, String headerText, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public void setSocketHandle(SocketHandle socketHandle) {
        this.socketHandle = socketHandle;
    }
}