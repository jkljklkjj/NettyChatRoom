package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private Server server;

    @Override
    @Async
    public void run(String... args) throws Exception {
        System.out.println("Netty服务端正在启动");
        server.start(); // 启动Netty服务端
    }
}