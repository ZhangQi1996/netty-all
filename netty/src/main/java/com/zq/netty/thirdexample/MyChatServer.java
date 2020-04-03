package com.zq.netty.thirdexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

public class MyChatServer {

    static class MyChatServerHandler extends SimpleChannelInboundHandler<String> {

        // channelGroup used to store all channels that have been conned.
        private static ChannelGroup channelGroup =
                // GlobalEventExecutor.INSTANCE is a mode of singleton
                // GlobalEventExecutor is an evt handler, it will launch this executor when no tasks are pended at que for 1 sec.
                new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            Channel channel = ctx.channel();
            String user = channel.remoteAddress().toString();
            String msg = String.format("[SERVER] User %s has been online...\n", user);
            // will travel all channels and send
            channelGroup.writeAndFlush(msg);

            channelGroup.add(channel);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            String user = ctx.channel().remoteAddress().toString();
            String msg = String.format("[SERVER] User %s has been offline...\n", user);
            // will travel all channels and send
            channelGroup.writeAndFlush(msg);
            // attention: do not need to invoke channelGroup.remove, as the underlying of netty will remove automatically.
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String user = ctx.channel().remoteAddress().toString();
            System.out.printf("User %s has been online...\n", user);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            String user = ctx.channel().remoteAddress().toString();
            System.out.printf("User %s has been offline...\n", user);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            Channel channel = ctx.channel();
            String user = channel.remoteAddress().toString();

            System.out.println("from cli " + channel.remoteAddress() + ": " + msg);
            channel.writeAndFlush("[SERVER] server has received your msg -- " + msg + "\n");
            channelGroup.writeAndFlush("[SERVER] User " + user + " has send the msg -- " + msg + "\n",
                    ch -> channel != ch);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            // close the channel when an ex happens.
            ctx.close();
        }
    }

    static class MyChatServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()),
                    new StringDecoder(CharsetUtil.UTF_8),
                    new StringEncoder(CharsetUtil.UTF_8),
                    new MyChatServerHandler()
            );
        }
    }


    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                    childHandler(new MyChatServerInitializer());
            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
