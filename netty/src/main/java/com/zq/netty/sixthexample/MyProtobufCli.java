package com.zq.netty.sixthexample;

import com.zq.proto.EducationOuter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class MyProtobufCli {

    static  class MyProtobufCliHandler extends SimpleChannelInboundHandler<EducationOuter.EducationUnion> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, EducationOuter.EducationUnion msg) throws Exception {
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
            ctx.writeAndFlush(EducationOuter.EducationUnion.newBuilder()
                    .setType(EducationOuter.EducationUnion.EducationType.STUDENT_TYPE)
                    .setStudent(
                            EducationOuter.Student.newBuilder()
                            .setName("David")
                            .setAge(20)
                            .setGrade("grade 1")
                            .build()
                    )
                    .build()
            );

            Thread.sleep(TimeUnit.SECONDS.toMillis(2));

            ctx.writeAndFlush(EducationOuter.EducationUnion.newBuilder()
                    .setType(EducationOuter.EducationUnion.EducationType.TEACHER_TYPE)
                    .setTeacher(
                            EducationOuter.Teacher.newBuilder()
                                    .setName("Hawking")
                                    .setAge(45)
                                    .setPosition("professor")
                                    .build()
                    )
                    .build()
            );
        }
    }

    static class MyProtobufCliInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();

            pipeline.addLast(
                    // the field LENGTH whose max bits are 32bits can represent 2^28 nums,
                    // using the encoding way of BASE-128-VARINTS.
                    // the first bit per byte is a flag bit meaning not end when it is 1 and it is the last byte when it is 0
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
                    new MyProtobufCliHandler()
            );
        }
    }

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new MyProtobufCliInitializer());

            ChannelFuture channelFuture = bootstrap.connect("localhost", 8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}
