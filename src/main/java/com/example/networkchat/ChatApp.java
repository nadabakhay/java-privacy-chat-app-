package com.example.networkchat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatApp extends JFrame {
    private final JTextPane chatArea;
    private final JTextField inputField;
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField userField;
    private final JComboBox<String> colorComboBox;
    private final JButton connectButton;
    private final JButton serverButton;
    private final JButton sendButton;

    private ChatClient client;
    private ChatServer server;
    private Thread receiverThread;
    private String userName = "Guest";
    private Color selectedColor = Color.BLUE;

    public ChatApp() {
        super("Java Chat GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        hostField = new JTextField("127.0.0.1");
        portField = new JTextField("9090");
        userField = new JTextField("Guest");
        colorComboBox = new JComboBox<String>(new String[] {"Blue", "Green", "Red", "Purple", "Orange", "Black"});
        colorComboBox.setSelectedItem("Blue");
        connectButton = new JButton("Connect");
        serverButton = new JButton("Start Server");

        topPanel.add(new JLabel("Host:"));
        topPanel.add(hostField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(new JLabel("User:"));
        topPanel.add(userField);
        topPanel.add(new JLabel("Color:"));
        topPanel.add(colorComboBox);
        topPanel.add(connectButton);
        topPanel.add(serverButton);

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        inputField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        colorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedColor = parseColor((String) colorComboBox.getSelectedItem());
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        serverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setSize(760, 560);
        setLocationRelativeTo(null);
    }

    private void startServer() {
        try {
            int port = ChatMessageUtil.parsePort(portField.getText(), 9090);
            if (server != null) {
                appendMessage("Server is already running.");
                return;
            }
            server = new ChatServer(port);
            server.start();
            appendMessage("Server started on port " + port);
        } catch (Exception ex) {
            appendMessage("Failed to start server: " + ex.getMessage());
        }
    }

    private void connectToServer() {
        try {
            userName = ChatMessageUtil.normalizeUserName(userField.getText());
            int port = ChatMessageUtil.parsePort(portField.getText(), 9090);
            client = new ChatClient(hostField.getText(), port);
            client.connect();
            appendMessage("Connected to " + hostField.getText() + ":" + port + " as " + userName);
            startReceiver();
        } catch (Exception ex) {
            appendMessage("Connection failed: " + ex.getMessage());
        }
    }

    private void startReceiver() {
        if (receiverThread != null && receiverThread.isAlive()) {
            return;
        }

        receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (client != null && client.isRunning()) {
                    try {
                        String message = client.receiveMessage();
                        if (message == null) {
                            break;
                        }
                        appendMessage(message);
                    } catch (IOException ex) {
                        appendMessage("Connection lost.");
                        break;
                    }
                }
            }
        });
        receiverThread.start();
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        if (client != null && client.isRunning()) {
            String formatted = ChatMessageUtil.formatMessage(userName, text);
            try {
                client.sendMessage(formatted);
                appendColoredMessage(formatted, selectedColor);
                inputField.setText("");
            } catch (RuntimeException ex) {
                appendColoredMessage("Message not sent: " + ex.getMessage(), Color.RED);
                client.disconnect();
            }
        } else {
            appendColoredMessage("Connect to a server before sending messages.", Color.RED);
        }
    }

    private void appendColoredMessage(String message, Color color) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                StyledDocument document = chatArea.getStyledDocument();
                SimpleAttributeSet attributes = new SimpleAttributeSet();
                StyleConstants.setForeground(attributes, color);
                try {
                    document.insertString(document.getLength(), message + "\n", attributes);
                } catch (javax.swing.text.BadLocationException ex) {
                    chatArea.setText(message + "\n");
                }
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });
    }

    private void appendMessage(String message) {
        appendColoredMessage(message, Color.BLACK);
    }

    private Color parseColor(String name) {
        if (name == null) {
            return Color.BLUE;
        }
        if (name.equalsIgnoreCase("green")) return Color.GREEN.darker();
        if (name.equalsIgnoreCase("red")) return Color.RED;
        if (name.equalsIgnoreCase("purple")) return new Color(128, 0, 128);
        if (name.equalsIgnoreCase("orange")) return new Color(255, 140, 0);
        if (name.equalsIgnoreCase("black")) return Color.BLACK;
        return Color.BLUE;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ChatApp().setVisible(true);
            }
        });
    }
}
