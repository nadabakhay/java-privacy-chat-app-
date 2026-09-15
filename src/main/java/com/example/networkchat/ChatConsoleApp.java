package com.example.networkchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ChatConsoleApp {
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("=== Console Chat ===");
        System.out.print("Enter your name: ");
        String user = ChatMessageUtil.normalizeUserName(reader.readLine());
        System.out.print("Enter host (default 127.0.0.1): ");
        String host = reader.readLine();
        if (host == null || host.trim().isEmpty()) {
            host = "127.0.0.1";
        }
        System.out.print("Enter port (default 9090): ");
        String portInput = reader.readLine();
        int port = ChatMessageUtil.parsePort(portInput, 9090);

        ChatClient client = new ChatClient(host, port);
        Thread receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (client.isRunning()) {
                        String message = client.receiveMessage();
                        if (message == null) {
                            break;
                        }
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            }
        });

        try {
            client.connect();
            client.sendMessage(ChatMessageUtil.formatMessage(user, "joined the chat"));
            System.out.println("Connected to " + host + ":" + port);
            receiverThread.start();
            String line;
            while ((line = reader.readLine()) != null) {
                if ("exit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                client.sendMessage(ChatMessageUtil.formatMessage(user, line));
            }
        } finally {
            client.disconnect();
            System.out.println("Disconnected.");
        }
    }
}
