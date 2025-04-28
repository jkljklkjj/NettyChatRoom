package com.example.handler.MessageHandlerImpl;

import com.example.handler.MessageHandler;
import com.example.constant.ServiceConstant;

/**
 * 工厂类，用于创建不同类型的消息处理器
 */
public class MessageHandlerFactory {
    public static MessageHandler create(String type) {
        return switch (type) {
            case ServiceConstant.CHAT -> new ChatHandler();
            case ServiceConstant.GROUP_CHAT -> new GroupChatHandler();
            case ServiceConstant.LOGIN -> new LoginHandler();
            case ServiceConstant.LOGOUT -> new LogoutHandler();
            case ServiceConstant.CHECK -> new CheckHandler();
            default -> null;
        };
    }
}