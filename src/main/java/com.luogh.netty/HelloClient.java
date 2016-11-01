package com.luogh.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.spark.unsafe.Platform;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static java.lang.System.out;

/**
 * @author luogh
 * @date 2016/10/1
 */
public class HelloClient {
    /**
     * 连接服务器
     * @param port
     * @param host
     * @throws Exception
     */
    public void connect(int port,String host) throws Exception {
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //客户端辅助启动类 对客户端配置
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()//.addLast("framer",new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()))
                                    .addLast("decoder",new StringDecoder()) // 字符串编码
                                    .addLast("encoder",new StringEncoder()) // 字符串解码
                                    .addLast(new HelloClientHandler());
                        }
                    });

            //异步链接服务器 同步等待链接成功
            ChannelFuture f = bootstrap.connect(host,port).sync();
            Channel ch = f.channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for(;;) {
                String line = in.readLine();
                if(line == null) continue;
                /**
                 * 向服务器端发送在控制台输入的文本,并用"\r\n"结尾
                 * 之所以使用\r\n结尾，是因为我们在handler中添加了DelimeterBasedFramDecorder
                 * 帧编码。
                 * 这个解码器是一个根据\n符号位分隔符的解码器，所以每条消息的最后必须加上\n\r,
                 * 否则无法识别和解码
                 */
                ch.writeAndFlush(line);
            }
        } finally {
            group.shutdownGracefully();
            out.println("客户端优雅的释放了资源...");
        }
    }

    public static void main(String[] args) throws Exception {
        new HelloClient().connect(8000,"127.0.0.1");
    }
}
