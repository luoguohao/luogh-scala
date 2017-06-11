package com.luogh.protoc.demo;

import com.luogh.protoc.demo.rpc.Message;
import com.google.protobuf.RpcCallback;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.client.DuplexTcpClientPipelineFactory;
import com.googlecode.protobuf.pro.duplex.client.RpcClientConnectionWatchdog;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * @author luogh
 */
public class Client {
    private static Logger LOG = LoggerFactory.getLogger(Client.class);

    private static RpcClientChannel channel = null;

    public static void main(String[] args) throws IOException {
        PeerInfo server = new PeerInfo("127.0.0.1", 1234);
        DuplexTcpClientPipelineFactory clientFactory = new DuplexTcpClientPipelineFactory();
        clientFactory.setConnectResponseTimeoutMillis(1000);
        clientFactory.setRpcServerCallExecutor(new ThreadPoolCallExecutor(3, 10));

        // 打开数据压缩
        clientFactory.setCompression(true);

        // RPC payloads are uncompression when logged - so reduce logging
        CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
        logger.setLogRequestProto(false);
        logger.setLogResponseProto(false);
        clientFactory.setRpcLogger(logger);

        // 回调
        final RpcCallback<Message.Msg> serverResponseCallback = new RpcCallback<Message.Msg>() {
            @Override
            public void run(Message.Msg parameter) {
                LOG.info("接收消息： " + parameter);
            }
        };

        // Set up the event pipeline factory.
        // setup a RPC event listener - it just logs what happens
        // 启动rpc事件监听
        RpcConnectionEventNotifier rpcEventNotifier = new RpcConnectionEventNotifier();
        RpcConnectionEventListener listener = new RpcConnectionEventListener() {
            @Override
            public void connectionReestablished(RpcClientChannel clientChannel) {
                channel = clientChannel;
                LOG.info("重新建立连接 " + clientChannel);
                clientChannel.setOobMessageCallback(Message.Msg.getDefaultInstance(), serverResponseCallback);
            }

            @Override
            public void connectionOpened(RpcClientChannel clientChannel) {
                channel = clientChannel;
                LOG.info("链接打开" + clientChannel);
                clientChannel.setOobMessageCallback(Message.Msg.getDefaultInstance(), serverResponseCallback);
            }

            @Override
            public void connectionLost(RpcClientChannel clientChannel) {
                LOG.info("链接断开" + clientChannel);
            }

            @Override
            public void connectionChanged(RpcClientChannel clientChannel) {
                channel = clientChannel;
                LOG.info("链接改变" + clientChannel);
            }
        };
        rpcEventNotifier.addEventListener(listener);
        clientFactory.registerConnectionEventListener(rpcEventNotifier);

        // 初始化netty
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workers = new NioEventLoopGroup(1, new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
        bootstrap.group(workers);
        bootstrap.handler(clientFactory);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048576);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048576);

        RpcClientConnectionWatchdog watchdog = new RpcClientConnectionWatchdog(clientFactory, bootstrap);
        rpcEventNotifier.addEventListener(watchdog);
        watchdog.start();

        CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
        shutdownHandler.addResource(workers);

        clientFactory.peerWith(server, bootstrap);

        while (true && channel != null) {
            // 创建消息
            Message.Msg msg = Message.Msg.newBuilder().setContent("Client " + channel +"OK@" + System.currentTimeMillis()).build();
            ChannelFuture oobSend = channel.sendOobMessage(msg);
            if (!oobSend.isDone()) {
                LOG.info("Waiting for completion.");
                oobSend.syncUninterruptibly();
            }
            if (!oobSend.isSuccess()) {
                LOG.warn("OobMessage send failed." + oobSend.cause());
            }

            try {
                LOG.info("Sleeping 5s before sending request to server.");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

}
