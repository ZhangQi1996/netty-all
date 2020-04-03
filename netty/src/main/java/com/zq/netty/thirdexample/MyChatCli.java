package com.zq.netty.thirdexample;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MyChatCli {

    static class MyChatCliHandler extends SimpleChannelInboundHandler<String> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println(msg);
        }

    }

    static class MyChatCliInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()),
                    new StringDecoder(CharsetUtil.UTF_8),
                    new StringEncoder(CharsetUtil.UTF_8),
                    new MyChatCliHandler()
            );
        }
    }

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new MyChatCliInitializer());

            Channel channel = bootstrap.connect("localhost", 8899).sync().channel();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                channel.writeAndFlush(reader.readLine() + "\n");
            }
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
