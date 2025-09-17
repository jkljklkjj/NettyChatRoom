package com.example.handler.MessageHandlerImpl;

import com.example.handler.MessageHandler;
import com.example.constant.ServiceConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 工厂类，用于创建不同类型的消息处理器
 */
@Component
public class MessageHandlerFactory {
    private static ApplicationContext applicationContext;

    @Autowired
    public void setApplicationContext(ApplicationContext context) {
        MessageHandlerFactory.applicationContext = context;
    }

    public static MessageHandler create(String type) {
        return switch (type) {
            case ServiceConstant.CHAT -> applicationContext.getBean(ChatHandler.class);
            case ServiceConstant.GROUP_CHAT -> applicationContext.getBean(GroupChatHandler.class);
            case ServiceConstant.LOGIN -> applicationContext.getBean(LoginHandler.class);
            case ServiceConstant.LOGOUT -> new LogoutHandler();
            case ServiceConstant.CHECK -> new CheckHandler();
            default -> null;
        };
    }
}