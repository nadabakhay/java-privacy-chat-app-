package com.example.networkchat;

public final class ChatMessageUtil {
    private ChatMessageUtil() {
    }

    public static String formatMessage(String user, String text) {
        if (user == null || user.trim().isEmpty()) {
            user = "Guest";
        }
        String safeText = text == null ? "" : text.trim();
        return user.trim() + ": " + safeText;
    }

    public static int parsePort(String input, int defaultPort) {
        try {
            int value = Integer.parseInt(input.trim());
            if (value > 0 && value <= 65535) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        return defaultPort;
    }

    public static String normalizeUserName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Guest" + System.currentTimeMillis();
        }
        return name.trim();
    }

    public static String normalizeRoomName(String room) {
        if (room == null || room.trim().isEmpty()) {
            return "public";
        }
        return room.trim();
    }
}
