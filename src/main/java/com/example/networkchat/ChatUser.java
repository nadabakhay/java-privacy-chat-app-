package com.example.networkchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatUser {
    private final String userName;
    private final Socket socket;
    private final BufferedReader input;
    private final PrintWriter output;

    public ChatUser(String userName, Socket socket) throws IOException {
        this.userName = ChatMessageUtil.normalizeUserName(userName);
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.output = new PrintWriter(socket.getOutputStream(), true);
    }

    public String getUserName() {
        return userName;
    }

    public void send(String message) {
        output.println(message);
    }

    public String readLine() throws IOException {
        return input.readLine();
    }

    public void close() throws IOException {
        input.close();
        output.close();
        socket.close();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatUser)) return false;
        ChatUser other = (ChatUser) obj;
        return userName.equals(other.userName);
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }
}
