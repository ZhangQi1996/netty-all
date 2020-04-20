package com.zq.netty.secondexample;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class MySocketCli {

    static  class MySocketCliHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            // print the addr of server
            System.out.println(ctx.channel().remoteAddress());
            System.out.println("cli received: " + msg);

            Thread.sleep(TimeUnit.SECONDS.toMillis(3));

            ctx.writeAndFlush("from cli: " + LocalDateTime.now());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush("from cli: the cli has conned");
        }
    }

    static class MySocketCliInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,
                    4, 0, 4))
                    .addLast(new LengthFieldPrepender(4))
                    .addLast(new StringDecoder(CharsetUtil.UTF_8))
                    .addLast(new StringEncoder(CharsetUtil.UTF_8))
                    .addLast(new MySocketCliHandler());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new MySocketCliInitializer());

            ChannelFuture channelFuture = bootstrap.connect("localhost", 5000).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
