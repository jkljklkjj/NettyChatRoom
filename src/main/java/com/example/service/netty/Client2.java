package com.example.service.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client2 {
    private static final Logger log = LoggerFactory.getLogger(Client2.class);
    private final String host;
    private final int port;

    public Client2(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                    log.info("接收到信息: {}", msg);
                                }
                            });
                        }
                    });

            Scanner sc = new Scanner(System.in);
            Channel channel = null;
            try {
                channel = b.connect(host, port).sync().channel();
                log.info("连接成功 host={} port={}", host, port);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch(Exception e) {
                log.error("无法连接到服务器 host={} port={}", host, port, e);
                return;
            }
            while (sc.hasNextLine()) {
                log.info("请输入您要发送给的用户(回车结束)：");
                String port = sc.nextLine();
                if(port.isEmpty()) break;
                String line = sc.nextLine();
                channel.writeAndFlush(port+"|"+line);
            }
        } catch (Exception e) {
            log.error("客户端运行异常", e);
        }
        finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Client2("localhost", 8080).start();
    }
}
