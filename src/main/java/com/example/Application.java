package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.boot.CommandLineRunner;

import com.example.service.netty.Server;

@SpringBootApplication
@MapperScan("com.example.mapper")
@EnableAsync
public class Application implements CommandLineRunner {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    @Async
    public void run(String... args) throws Exception {
        System.out.println("Netty服务端正在启动");
        Server server = new Server(); // 使用8080端口作为Netty服务端的监听端口
        server.start(); // 启动Netty服务端
    }
}