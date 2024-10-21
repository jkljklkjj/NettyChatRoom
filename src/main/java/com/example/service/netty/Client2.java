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

public class Client2 {
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
                                    System.out.println("接收到信息："+msg);
                                }
                            });
                        }
                    });

            Scanner sc = new Scanner(System.in);
            Channel channel = null;
            try {
                channel = b.connect(host, port).sync().channel();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch(Exception e) {
                System.out.println("无法连接到服务器，你和服务器肯定有个有问题哈哈");
                return;
            }
            while (sc.hasNextLine()) {
                System.out.print("请输入您要发送给的用户：");
                String port = sc.nextLine();
                if(port.isEmpty()) break;
                String line = sc.nextLine();
                channel.writeAndFlush(port+"|"+line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new Client2("localhost", 8080).start();
    }
}
