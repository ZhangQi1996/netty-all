package com.zq.netty.fourthexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class MyHeartbeatServer {

    static class MyHeartbeatServerHandler extends ChannelInboundHandlerAdapter {
        //
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;

                String eventType = null;

                switch (event.state()) {
                    case READER_IDLE:
                        eventType = "reader idle";
                        break;
                    case WRITER_IDLE:
                        eventType = "writer idle";
                        break;
                    case ALL_IDLE:
                        eventType = "all idle";
                        break;
                }

                System.out.println(ctx.channel().remoteAddress() + "timeout evt: " + eventType);

                ctx.channel().close();

            }
        }
    }

    static class MyHeartbeatServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(
                    new IdleStateHandler(5, 7, 10, TimeUnit.SECONDS),
                    new MyHeartbeatServerHandler()
            );
        }
    }



    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO)) // 针对boss group
                    .childHandler(new MyHeartbeatServerInitializer()); // 针对worker group
            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
