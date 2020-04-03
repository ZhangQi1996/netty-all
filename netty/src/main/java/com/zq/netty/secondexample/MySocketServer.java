package com.zq.netty.secondexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MySocketServer {

    static class MySocketServerHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            // print the addr od cli
            System.out.println(ctx.channel().remoteAddress() + ", " + msg);

            Thread.sleep(TimeUnit.SECONDS.toMillis(3));

            ctx.channel().writeAndFlush("from server: " + UUID.randomUUID());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            // close the channel when an ex happens
            ctx.close();
        }
    }

    static class MySocketServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                    4, 0, 4))
                    .addLast(new LengthFieldPrepender(4))
                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                    .addLast(new MySocketServerHandler());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup parent = new NioEventLoopGroup();
        NioEventLoopGroup child = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(parent, child)
                    .channel(NioServerSocketChannel.class)
                    // .handler(null); // deal with sth at parent event loop group, e.g. do logging
                    // childHandler is that do sth at child event loop group
                    .childHandler(new MySocketServerInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            parent.shutdownGracefully();
            child.shutdownGracefully();
        }

    }
}
