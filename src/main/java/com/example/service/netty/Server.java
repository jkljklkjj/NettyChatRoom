package com.example.service.netty;

import com.example.handler.TimeoutHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import com.example.handler.HttpServerHandler;
import com.example.handler.JwtRequestHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

@Component
public class Server {
    private static final int PORT = 8080;

    public Server() {
    }

    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        // TODO 添加读写检测器，超过五分钟触发离线机制
        // TODO 同时在前端也添加，然后触发重入机制
        // TODO 把协议修正成websocket
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(300, 0, 0)); // 读超时 300 秒
                            ch.pipeline().addLast(new TimeoutHandler());
                            ch.pipeline().addLast(new HttpRequestDecoder());
                            ch.pipeline().addLast(new HttpObjectAggregator(65536));
                            ch.pipeline().addLast(new HttpResponseDecoder());
                            ch.pipeline().addLast(new HttpResponseEncoder());
                            ch.pipeline().addLast(new JwtRequestHandler());
//                            ch.pipeline().addLast(new HttpRequestEncoder());
                            ch.pipeline().addLast(new HttpServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(PORT).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        System.out.println("服务器启动了哦！！");
        new Thread(new Server()::start).start();
    }
}