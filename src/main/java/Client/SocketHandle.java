package Client;

import java.io.*;
import java.net.Socket;

public class SocketHandle {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public SocketHandle(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        initializeStreams();
    }

    private void initializeStreams() throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public synchronized void sendMessage(String message) throws IOException {
        if (out == null) {
            throw new IOException("Output stream is not initialized.");
        }
        out.println(message);
    }

    public synchronized String receiveMessage() throws IOException {
        if (in == null) {
            throw new IOException("Input stream is not initialized.");
        }
        return in.readLine();
    }

    public synchronized void close() {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    public void reconnect(String host, int port) throws IOException {
        close();
        this.socket = new Socket(host, port);
        initializeStreams();
    }
}