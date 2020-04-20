package com.zq.nio;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Test {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        SelectionKey key1 = socketChannel.register(selector,
                SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        System.out.println(key1.interestOps() + " " + key1.readyOps());

    }
}
