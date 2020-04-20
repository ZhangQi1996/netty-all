package com.zq.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.stream.IntStream;

public class Test7 {
    // nio非阻塞编程, 生成5个基于nio的服务器连接端口，每个服务器端口又支持多个cli连接
    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        for (int port : IntStream.rangeClosed(5000, 5004).toArray()) {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);

            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(port));

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("listening: " + port);
        }
        ;

        while (true) {
            int keysNum = selector.select();
            System.out.println("the num of keys is " + keysNum);

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            System.out.println("selectionKeys: " + selectionKeys);

            for (SelectionKey key : selectionKeys) {
                // key contains SelectionKey.OP_ACCEPT
                if (key.isAcceptable()) {
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);

                    socketChannel.register(selector, SelectionKey.OP_READ);


                    System.out.println("obtaining the cli's conn: " + socketChannel);
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(512);
                    int len = 0;
                    int sumLen = 0;
                    // in general, it should be larger and equal to zero
                    while ((len = socketChannel.read(byteBuffer)) > 0) {
                        sumLen += len;
                    }; // read op
                    // balabala
                    byteBuffer.flip(); // flip to write

                    socketChannel.write(byteBuffer);
                    byteBuffer.clear();

                    System.out.println("read in sum: " + sumLen + ", from: " + socketChannel);
                }
            }
            // remove all keys from set.
            selectionKeys.clear();
        }
    }
}
