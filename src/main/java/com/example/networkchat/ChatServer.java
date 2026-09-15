package com.example.networkchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private final int port;
    private final String passphrase;
    private ServerSocket serverSocket;
    private volatile boolean running = false;
    private final List<ClientHandler> clients = new ArrayList<ClientHandler>();

    public ChatServer(int port) {
        this(port, "privacy-default-passphrase");
    }

    public ChatServer(int port, String passphrase) {
        this.port = port;
        this.passphrase = PrivacyCrypto.normalizePassphrase(passphrase);
    }

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? ChatMessageUtil.parsePort(args[0], 9090) : 9090;
        final ChatServer chatServer = new ChatServer(port);
        chatServer.start();
        System.out.println("Chat server listening on port " + port);
        System.out.println("Press Ctrl+C to stop the server.");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                chatServer.stop();
            }
        }));
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        Thread acceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        Socket socket = serverSocket.accept();
                        ClientHandler handler = new ClientHandler(socket);
                        synchronized (clients) {
                            clients.add(handler);
                        }
                        Thread t = new Thread(handler);
                        t.start();
                    } catch (IOException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        acceptThread.start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
        }
    }

    public void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.send(message);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final String connectionPassphrase;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.connectionPassphrase = passphrase;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void send(String message) {
            if (out != null) {
                try {
                    out.println(PrivacyCrypto.encrypt(message, connectionPassphrase));
                } catch (GeneralSecurityException e) {
                    out.println(message);
                }
            }
        }

        public void close() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    try {
                        String decrypted = PrivacyCrypto.decrypt(line, connectionPassphrase);
                        broadcast(decrypted);
                    } catch (GeneralSecurityException e) {
                        broadcast(line);
                    }
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
            } finally {
                synchronized (clients) {
                    clients.remove(this);
                }
                close();
            }
        }
    }
}
