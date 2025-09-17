package com.example.service.netty;

import com.example.handler.TimeoutHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.example.handler.HttpServerHandler;
import com.example.handler.JwtRequestHandler;
import com.example.service.security.JwtService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    @Value("${netty.port:8080}")
    private int port;

    private final JwtService jwtService;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private Channel serverChannel;
    private final CompletableFuture<Void> startFuture = new CompletableFuture<>();
    private volatile boolean started = false;

    @Autowired
    public Server(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostConstruct
    public void start() {
        if (started) {
            return;
        }
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new IdleStateHandler(300, 0, 0));
                            ch.pipeline().addLast(new TimeoutHandler());
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new HttpResponseDecoder());
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            ch.pipeline().addLast(new JwtRequestHandler(jwtService));
                            ch.pipeline().addLast(new HttpServerHandler());
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_BACKLOG, 128);

            log.info("Netty Server 正在绑定端口 {}...", port);
            b.bind(port).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    serverChannel = future.channel();
                    started = true;
                    log.info("Netty Server 启动成功, 端口 {}", port);
                    startFuture.complete(null);
                } else {
                    log.error("Netty Server 启动失败", future.cause());
                    startFuture.completeExceptionally(future.cause());
                }
            });
        } catch (Exception e) {
            log.error("Netty Server 启动异常", e);
            startFuture.completeExceptionally(e);
        }
    }

    public boolean isStarted() {
        return started && serverChannel != null && serverChannel.isActive();
    }

    public void awaitStarted(long timeout, TimeUnit unit) throws Exception {
        startFuture.get(timeout, unit);
    }

    @PreDestroy
    public void stop() {
        if (!started) {
            return;
        }
        log.info("Netty Server 正在关闭...");
        try {
            if (serverChannel != null) {
                serverChannel.close().syncUninterruptibly();
            }
        } catch (Exception e) {
            log.warn("关闭 serverChannel 发生异常", e);
        } finally {
            if (bossGroup != null) bossGroup.shutdownGracefully();
            if (workerGroup != null) workerGroup.shutdownGracefully();
            started = false;
            log.info("Netty Server 已关闭");
        }
    }
}