package com.example.handler.MessageHandlerImpl;

import com.example.handler.MessageHandler;

public class MessageHandlerFactory {
    public static MessageHandler create(String type) {
        return switch (type) {
            case "chat" -> new ChatHandler();
            case "login" -> new LoginHandler();
            case "logout" -> new LogoutHandler();
            default -> null;
        };
    }
}