package com.zq.nio;

import com.zq.aux_bean.JudgementsChain;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NioServer {

    private static Map<String, SocketChannel> map = new HashMap<>();

    // 一个主线程，完成所有连接请求
    // 主要面向于那种并发量大，而每次请求需要处理的时间少的情况

    // 通过将接收连接请求的serverSockChannel与处理连接的socketChannel都注册在一个selector上
    // 每当一个新的连接请求来到，就会触发serverSockChannel的accept操作，然后就会有accept() ->socketChannel
    // 再将每个新的socketChannel注册到selector上
    // 所有channel都设置为非阻塞
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // Conf non-blocking mode
        serverSocketChannel.configureBlocking(false);
        // Conf the port that the socket binds
        serverSocketChannel.socket().bind(new InetSocketAddress(8899));

        // New a selector
        Selector selector = Selector.open();
        // Reg this channel on the selector, and returns a key
        // representing the registration of this channel with the given selector
        // The channel serverSocketChannel focuses on the event accept.
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        JudgementsChain<SelectionKey> judgementsChain = JudgementsChain.generateInstance();
        judgementsChain.addJudgement(SelectionKey::isAcceptable, selectionKey -> {
            try {
                ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                SocketChannel socketChannel = channel.accept();
                socketChannel.configureBlocking(false);
                // The channel socketChannel focuses on the event read.
                socketChannel.register(selector, SelectionKey.OP_READ);

                String key = socketChannel.getRemoteAddress().toString();
                System.out.println(key + " has conned...");
                map.put(key, socketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).addJudgement(SelectionKey::isReadable, selectionKey -> {
            try {
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                ByteBuffer buffer = ByteBuffer.allocate(512);

                // indicates a read-turn
                while (channel.read(buffer) > 0) ;

                buffer.flip();

                String recvedMsg = String.valueOf(StandardCharsets.UTF_8.decode(buffer).array());
                buffer.clear();

                System.out.println("server has recved the msg: 【" + recvedMsg + "】from" + channel.getRemoteAddress());

                map.forEach((key, value) -> {
                    try {
                        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                        writeBuffer.put(String.format("%s has send: %s", channel.getRemoteAddress(), recvedMsg)
                                .getBytes(StandardCharsets.UTF_8));
                        writeBuffer.flip();
                        value.write(writeBuffer);
                        writeBuffer.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

                selectionKey.interestOps(SelectionKey.OP_WRITE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).addJudgement(SelectionKey::isWritable, selectionKey -> {
            System.out.println(selectionKey.interestOps() + " " + selectionKey.readyOps());
        });

        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            selectionKeys.forEach(judgementsChain::run);
            selectionKeys.clear();
        }
    }
}
