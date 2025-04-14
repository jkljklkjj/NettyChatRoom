package com.example.handler.MessageHandlerImpl;

import com.example.handler.MessageHandler;
import com.example.constant.ServiceConstant;

public class MessageHandlerFactory {
    public static MessageHandler create(String type) {
        return switch (type) {
            case ServiceConstant.CHAT -> new ChatHandler();
            case ServiceConstant.LOGIN -> new LoginHandler();
            case ServiceConstant.LOGOUT -> new LogoutHandler();
            default -> null;
        };
    }
}