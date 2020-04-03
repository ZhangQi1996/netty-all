package com.zq.netty.firstexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class TestServer {

    static class TestServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast("httpServerCodec", new HttpServerCodec());
            pipeline.addLast("httpServerHandler", new TestHttpServerHandler());
        }
    }

    static class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
        // read the reqs fom the cli, and return the rsp
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {

            Thread.sleep(TimeUnit.SECONDS.toMillis(1));

            if (msg instanceof HttpRequest) {
                ByteBuf content = Unpooled.copiedBuffer("hello world", CharsetUtil.UTF_8);
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK, content);

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain")
                        .set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

                ctx.writeAndFlush(response);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channel active");
            super.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channel inactive");
            super.channelInactive(ctx);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channel registered");
            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channel unregistered");
            System.out.println("---------------------");
            super.channelUnregistered(ctx);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("handler added");
            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            System.out.println("handler removed");
            super.handlerRemoved(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("channel read");
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channel read complete");
            super.channelReadComplete(ctx);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // get conn, and dispatch to workers
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        // 处理连接
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TestServerInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


}
