package com.luogh.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

import static java.lang.System.out;

/**
 * server端网络IO事件处理
 * @author luogh
 * @date 2016/10/1
 */
public class HelloServerHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        out.println("服务器端读取到客户端请求...");
        out.println("the hello server receive client: ["+ctx.channel().remoteAddress()+"] order:"+msg);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(msg)? new Date(System.currentTimeMillis())
                .toString(): "BAD ORDER";
        ctx.write(currentTime);
        out.println("服务器端做出了响应");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        out.println("服务器端readComplete 响应完成.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception {
        ctx.close();
        out.println("服务器端异常退出"+cause.getMessage());
    }

}
