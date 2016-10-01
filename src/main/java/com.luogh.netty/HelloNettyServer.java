package com.luogh.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import static java.lang.System.out;

/**
 * @author luogh
 * @date 2016/10/1
 */
public class HelloNettyServer {
    public void bind(int port) throws Exception {
        // 服务器线程组,用于网络事件的处理，一个用于服务器接收客户端的连接，另一个用于处理
        // SocketChannel的网络读写
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //NIO 服务器端的辅助启动类 降低开发难度
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class) //类似于NIO中的ServerSocketChannel
                    .option(ChannelOption.SO_BACKLOG,1024) //配置TCP参数
                    .childHandler(new ChildChannelHandler()); //绑定I/O事件的处理类

            //服务器端启动后 绑定监听端口 同步等待成功 主要是用于异步操作的通知回调 回调处理用的
            // ChildChannelHandler
            ChannelFuture f = serverBootstrap.bind(port).sync();
            out.println("Hello Netty Server started.");
            //等待服务器端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            out.println("服务器端优雅的释放了线程资源....");
        }
    }

    /**
     * 网络事件处理器
     */
    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            //以("\n")为结尾分隔的解码器
            ch.pipeline()//.addLast("framer",new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                    .addLast("decoder",new StringDecoder()) // 字符串编码
                    .addLast("encoder",new StringEncoder()) // 字符串解码
                    .addLast("handler",new HelloServerHandler()); // 自己的逻辑Handler
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8000;
        new HelloNettyServer().bind(port);
    }
}
