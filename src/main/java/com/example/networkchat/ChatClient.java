package com.example.networkchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.security.GeneralSecurityException;

public class ChatClient {
    private final String host;
    private final int port;
    private final String passphrase;
    private final String socksHost;
    private final int socksPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = false;

    public ChatClient(String host, int port) {
        this(host, port, "privacy-default-passphrase", null, 0);
    }

    public ChatClient(String host, int port, String passphrase) {
        this(host, port, passphrase, null, 0);
    }

    public ChatClient(String host, int port, String passphrase, String socksHost, int socksPort) {
        this.host = host;
        this.port = port;
        this.passphrase = PrivacyCrypto.normalizePassphrase(passphrase);
        this.socksHost = socksHost;
        this.socksPort = socksPort;
    }

    public void connect() throws IOException {
        Socket socketToUse = createSocket();
        this.socket = socketToUse;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        running = true;
    }

    public void disconnect() {
        running = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (out == null || !running) {
            throw new IllegalStateException("Client is not connected");
        }
        try {
            String encrypted = PrivacyCrypto.encrypt(message, passphrase);
            out.println(encrypted);
            if (out.checkError()) {
                running = false;
                throw new IllegalStateException("Connection to server was lost");
            }
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not encrypt message", e);
        }
    }

    public String receiveMessage() throws IOException {
        if (in == null) {
            return null;
        }
        String encrypted = in.readLine();
        if (encrypted == null || encrypted.trim().isEmpty()) {
            return encrypted;
        }
        try {
            return PrivacyCrypto.decrypt(encrypted, passphrase);
        } catch (GeneralSecurityException e) {
            return encrypted;
        }
    }

    public boolean isRunning() {
        return running;
    }

    private Socket createSocket() throws IOException {
        if (socksHost != null && !socksHost.trim().isEmpty() && socksPort > 0) {
            Socket proxied = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksHost, socksPort)));
            proxied.connect(new InetSocketAddress(host, port), 15000);
            return proxied;
        }
        return new Socket(host, port);
    }
}
