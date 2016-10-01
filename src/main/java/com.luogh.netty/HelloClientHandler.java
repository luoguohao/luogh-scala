package com.luogh.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Client 网络IO事件处理
 * @author luogh
 * @date 2016/10/1
 */
public class HelloClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        out.println("客户端active");
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        out.println("客户端接收到服务器响应数据");
        out.println("Now is :"+msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
        out.println("客户端接收到服务器端的数据处理完成");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        out.println("Client closed");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause) throws Exception {
        err.println("Unexpected Exception from downstream :"+cause.getMessage());
        ctx.close();
        out.println("客户端异常退出");
    }
}
