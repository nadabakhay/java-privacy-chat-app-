package com.example.networkchat;

import java.util.ArrayList;
import java.util.List;

public class ChatRoom {
    private final String name;
    private final List<ChatUser> users = new ArrayList<ChatUser>();

    public ChatRoom(String name) {
        this.name = ChatMessageUtil.normalizeRoomName(name);
    }

    public String getName() {
        return name;
    }

    public synchronized void join(ChatUser user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public synchronized void leave(ChatUser user) {
        users.remove(user);
    }

    public synchronized List<ChatUser> getUsers() {
        return new ArrayList<ChatUser>(users);
    }

    public synchronized void broadcast(String message, ChatUser sender) {
        for (ChatUser user : users) {
            if (user != sender) {
                user.send(message);
            }
        }
    }
}
