package Server;

import Client.SocketHandle;
import DAO.DatabaseUtil;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import Controller.FXMLDocumentController;

public class ServerThread extends Thread {
    private Connection connection;
    private Socket clientSocket;
    private Model.flowersData flowersData;
    private SocketHandle socketHandle;

    public ServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Received: " + message);

                String[] parts = message.split(":");
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        handleLogin(out, parts);
                        break;
                    case "REGISTER":
                        handleRegister(out, parts);
                        break;
                    case "BUY":
                        handlePayment(out, parts);
                        break;
                    default:
                        out.println("INVALID_COMMAND");
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRegister(PrintWriter out, String[] parts) {
        if (parts.length == 3 && "REGISTER".equals(parts[0])) {
            String username = parts[1];
            String password = parts[2];

            boolean isRegistered = registerUser(username, password);
            if (isRegistered) {

                out.println("REGISTER_SUCCESS");
            } else {
                out.println("REGISTER_FAILED");
            }
        } else {
            out.println("INVALID_COMMAND");
        }
    }


//    private void handleBuyCommand(PrintWriter out, String[] parts) {
//        // Kiểm tra độ dài của mảng parts để đảm bảo các tham số cần thiết được cung cấp
//        if (parts.length < 3) {
//            out.println("BUY_FAILED: Missing parameters");
//            return;
//        }
//        // Lấy ID sản phẩm và số lượng từ mảng parts
//        int flowerId;
//        int quantity;
//
//        try {
//            flowerId = Integer.parseInt(parts[1]);
//            quantity = Integer.parseInt(parts[2]);
//        } catch (NumberFormatException e) {
//            out.println("BUY_FAILED: Invalid parameters");
//            return;
//        }
//
//        // Kiểm tra thông tin sản phẩm
//        flowersData flower = findFlowerById(flowerId);
//        if (flower == null) {
//            out.println("BUY_FAILED: Product not found");
//            return;
//        }
//
//        // Kiểm tra trạng thái hàng tồn kho
//        if (!flower.getStatus().equals("Not Available")) {
//            out.println("BUY_FAILED: Product is out of stock");
//            return;
//        }
//
//        // Gửi phản hồi thành công
//        out.println("BUY_SUCCESS: Product " + flowerId + " purchased, quantity: " + quantity);
//    }

//    private flowersData findFlowerById(int flowerId) {
//        String sql = "SELECT * FROM flowers WHERE flower_id = ?";
//        flowersData flower = null;
//
//        try (PreparedStatement prepare = connection.prepareStatement(sql)) {
//            prepare.setInt(1, flowerId);
//            ResultSet result = prepare.executeQuery();
//
//            if (result.next()) {
//                flower = new flowersData(
//                        result.getInt("flower_id"),
//                        result.getString("name"),
//                        result.getString("status"),
//                        result.getDouble("price"),
//                        result.getString("image"),
//                        result.getDate("date")
//                );
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return flower;
//    }

    private void handlePayment(PrintWriter out, String[] parts) {
        if (parts.length < 3) {
            out.println("PAYMENT_FAILED: Missing parameters");
            return;
        }

        int customerId;
        double totalAmount;

        try {
            customerId = Integer.parseInt(parts[1]);
            totalAmount = Double.parseDouble(parts[2]);
        } catch (NumberFormatException e) {
            out.println("PAYMENT_FAILED: Invalid parameters");
            return;
        }

        if (totalAmount <= 0) {
            out.println("PAYMENT_FAILED: Invalid total amount");
            return;
        }

        boolean success = processPayment(customerId, totalAmount);

        if (success) {
            out.println("PAYMENT_SUCCESS"); // Phản hồi chính xác cho thanh toán thành công
        } else {
            out.println("PAYMENT_FAILED: Payment failed");
        }
    }

    private boolean processPayment(int customerId, double totalAmount) {
        String sql = "INSERT INTO customer_info (customer_id, total, date) VALUES (?, ?, ?)";
        boolean success = false;

        try (PreparedStatement prepare = connection.prepareStatement(sql)) {
            prepare.setInt(1, customerId);
            prepare.setDouble(2, totalAmount);

            Date date = new Date();
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());
            prepare.setDate(3, sqlDate);

            int rowsInserted = prepare.executeUpdate();
            success = (rowsInserted > 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return success;
    }


//    private void handleRegister(PrintWriter out, String[] parts) {
//        if (parts.length == 3 && "REGISTER".equals(parts[0])) {
//            String username = parts[1];
//            String password = parts[2];
//
//            boolean isRegistered = registerUser(username, password);
//            if (isRegistered) {
//                socketHandle.sendMessage("");
//            } else {
//                socketHandle.sendMessage("REGISTER_FAILED");
//            }
//        } else {
//            socketHandle.sendMessage("INVALID_COMMAND");
//        }
//    }


    private void handleLogin(PrintWriter out, String[] parts) {
        if (parts.length == 3 && "LOGIN".equals(parts[0])) {
            String username = parts[1];
            String password = parts[2];
            if (username != null && username.length() >= 3) {
                String user = username.substring(0, 3);

                if (validateUser(username, password)) {
                    out.println("LOGIN_SUCCESS");
                } else {
                    out.println("LOGIN_FAILED");
                }
            } else {
                out.println("INVALID_COMMAND");
            }
        }
    }

    private boolean validateUser(String username, String password) {
        boolean isValidUser = false;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            // Mã hóa mật khẩu nhập vào từ người dùng
            String hashedPassword = passwordEncoder.encode(password);

            String sql = "SELECT password FROM admin WHERE username = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPasswordHash = resultSet.getString("password");

                // So sánh mật khẩu đã mã hóa từ người dùng với mật khẩu đã lưu
                if (passwordEncoder.matches(password, storedPasswordHash)) {
                    isValidUser = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isValidUser;
    }

    private boolean registerUser(String username, String password) {
        boolean isRegistered = false;
        Connection connection = null;
        PreparedStatement checkStatement = null;
        PreparedStatement insertStatement = null;
        ResultSet resultSet = null;

        try {
            connection = DatabaseUtil.getConnection();

            // Kiểm tra xem username đã tồn tại trong CSDL chưa
            String checkSql = "SELECT COUNT(*) FROM admin WHERE username = ?";
            checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setString(1, username);
            resultSet = checkStatement.executeQuery();

            if (resultSet.next() && resultSet.getInt(1) == 0) { // Nếu username chưa tồn tại
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12)); // Hash mật khẩu
                String insertSql = "INSERT INTO admin (username, password) VALUES (?, ?)";
                insertStatement = connection.prepareStatement(insertSql);

                insertStatement.setString(1, username);
                insertStatement.setString(2, hashedPassword);

                int rowsInserted = insertStatement.executeUpdate();
                isRegistered = (rowsInserted > 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (checkStatement != null) checkStatement.close();
                if (insertStatement != null) insertStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return isRegistered;
    }



}
