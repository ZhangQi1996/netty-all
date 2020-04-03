package com.zq.netty.sixthexample;

import com.zq.proto.EducationOuter;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class MyProtobufServer {


    static class MyProtobufServerHandler extends SimpleChannelInboundHandler<EducationOuter.EducationUnion> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, EducationOuter.EducationUnion msg) throws Exception {
            if (msg.isInitialized()) {
                switch (msg.getType()) {
                    case STUDENT_TYPE:
                        EducationOuter.Student student = msg.getStudent();
                        System.out.println(student);
                        break;
                    case SCHOOL_TYPE:
                        EducationOuter.School school = msg.getSchool();
                        System.out.println(school);
                        break;
                    case TEACHER_TYPE:
                        EducationOuter.Teacher teacher = msg.getTeacher();
                        System.out.println(teacher);
                        break;
                    default:
                        throw new IllegalStateException();
                }

            }
        }
    }

    static class MyProtobufServerInitializer extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(
                    /*
                     * BEFORE DECODE (302 bytes)       AFTER DECODE (300 bytes)
                     * +--------+---------------+      +---------------+
                     * | Length | Protobuf Data |----->| Protobuf Data |
                     * | 0xAC02 |  (300 bytes)  |      |  (300 bytes)  |
                     * +--------+---------------+      +---------------+
                    */
                    new ProtobufVarint32FrameDecoder(),
                    // protobuf data -> protobuf obj
                    new ProtobufDecoder(EducationOuter.EducationUnion.getDefaultInstance()),
                    /*
                     * BEFORE ENCODE (300 bytes)       AFTER ENCODE (302 bytes)
                     * +---------------+               +--------+---------------+
                     * | Protobuf Data |-------------->| Length | Protobuf Data |
                     * |  (300 bytes)  |               | 0xAC02 |  (300 bytes)  |
                     * +---------------+               +--------+---------------+
                     * */
                    new ProtobufVarint32LengthFieldPrepender(),
                    // protobuf obj -> protobuf data
                    new ProtobufEncoder(),
                    new MyProtobufServerHandler()
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
                    .handler(new LoggingHandler(LogLevel.INFO)) // for boss group
                    .childHandler(new MyProtobufServerInitializer()); // for worker group
            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
