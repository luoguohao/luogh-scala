package cn.slimsmart.protoc.demo;

import cn.slimsmart.protoc.demo.rpc.Message;
import com.google.protobuf.RpcCallback;
import com.googlecode.protobuf.pro.duplex.CleanShutdownHandler;
import com.googlecode.protobuf.pro.duplex.PeerInfo;
import com.googlecode.protobuf.pro.duplex.RpcClientChannel;
import com.googlecode.protobuf.pro.duplex.RpcConnectionEventNotifier;
import com.googlecode.protobuf.pro.duplex.execute.RpcServerCallExecutor;
import com.googlecode.protobuf.pro.duplex.execute.ThreadPoolCallExecutor;
import com.googlecode.protobuf.pro.duplex.listener.RpcConnectionEventListener;
import com.googlecode.protobuf.pro.duplex.logging.CategoryPerServiceLogger;
import com.googlecode.protobuf.pro.duplex.server.DuplexTcpServerPipelineFactory;
import com.googlecode.protobuf.pro.duplex.util.RenamingThreadFactoryProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author luogh
 */
public class Server {
    private static Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        PeerInfo serverInfo = new PeerInfo("127.0.0.0", 1234);

        // RPC payloads are uncompressed when logged - so reduce logging
        // 关闭 减少日志 或者 com.googlecode.protobuf.pro.duplex.logging.nulllogger可以替代的，
        // 将不记录任何categoryperservicelogger.
        CategoryPerServiceLogger logger = new CategoryPerServiceLogger();
        logger.setLogRequestProto(false);
        logger.setLogResponseProto(false);

        // 配置server
        DuplexTcpServerPipelineFactory serverFactory = new DuplexTcpServerPipelineFactory(serverInfo);
        // 设置线程池
        RpcServerCallExecutor rpcExecutor = new ThreadPoolCallExecutor(10, 10);
        serverFactory.setRpcServerCallExecutor(rpcExecutor);
        serverFactory.setLogger(logger);

        //回调
        final RpcCallback<Message.Msg> clientResponseCallback = new RpcCallback<Message.Msg>() {
            @Override
            public void run(Message.Msg parameter) {
                LOG.info("接收:" + parameter);
            }
        };

        // 启动rpc事件监听
        RpcConnectionEventNotifier rpcConnectionEventNotifier = new RpcConnectionEventNotifier();
        RpcConnectionEventListener listener = new RpcConnectionEventListener() {
            @Override
            public void connectionReestablished(RpcClientChannel clientChannel) {
                LOG.info("重新建立连接:"+ clientChannel);
                clientChannel.setOobMessageCallback(Message.Msg.getDefaultInstance(), clientResponseCallback);
            }

            @Override
            public void connectionOpened(RpcClientChannel clientChannel) {
                LOG.info("连接打开:" + clientChannel);
                clientChannel.setOobMessageCallback(Message.Msg.getDefaultInstance(), clientResponseCallback);
            }

            @Override
            public void connectionLost(RpcClientChannel clientChannel) {
                LOG.info("连接断开: "+ clientChannel);
            }

            @Override
            public void connectionChanged(RpcClientChannel clientChannel) {
                LOG.info("链接改变:" + clientChannel);
            }
        };

        rpcConnectionEventNotifier.setEventListener(listener);
        serverFactory.registerConnectionEventListener(rpcConnectionEventNotifier);

        // 初始化netty
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup boss = new NioEventLoopGroup(2, new RenamingThreadFactoryProxy("boss", Executors.defaultThreadFactory()));
        EventLoopGroup workers = new NioEventLoopGroup(1, new RenamingThreadFactoryProxy("worker", Executors.defaultThreadFactory()));
        bootstrap.group(boss, workers);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1048578);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1048578);
        bootstrap.childOption(ChannelOption.SO_RCVBUF, 1048578);
        bootstrap.childOption(ChannelOption.SO_SNDBUF, 1048578);

        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.childHandler(serverFactory);
        bootstrap.localAddress(serverInfo.getPort());

        // 关闭释放资源
        CleanShutdownHandler shutdownHandler = new CleanShutdownHandler();
        shutdownHandler.addResource(boss);
        shutdownHandler.addResource(workers);
        shutdownHandler.addResource(rpcExecutor);

        bootstrap.bind();
        LOG.info("启动监听:" + bootstrap);

        // 定时向客户端发送 消息
        while (true) {
            List<RpcClientChannel> clients = serverFactory.getRpcClientRegistry().getAllClients();
            for (RpcClientChannel client : clients) {
                //创建消息
                Message.Msg msg = Message.Msg.newBuilder().setContent("Server" + serverFactory.getServerInfo() + "OK@" + System.currentTimeMillis()).build();
                ChannelFuture oobSend = client.sendOobMessage(msg);
                if (!oobSend.isDone()) {
                    LOG.info("Waiting for completion.");
                    oobSend.syncUninterruptibly();
                }
                if (!oobSend.isSuccess()) {
                    LOG.warn("OobMessage send failed." + oobSend.cause());
                }
            }
            LOG.info("sleeping 5s before sending request to all clients.");
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
