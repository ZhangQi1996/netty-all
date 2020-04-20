package com.zq.nio;

import com.zq.aux_bean.JudgementsChain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NioCli {

    static JudgementsChain<SelectionKey> getMyJudgementsChain(Selector selector) {
        JudgementsChain<SelectionKey> judgementsChain = JudgementsChain.generateInstance();
        judgementsChain.addJudgement(SelectionKey::isConnectable, key -> {
            try {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                if (socketChannel.isConnectionPending()) {
                    boolean finishConnect = socketChannel.finishConnect();// maybe return false
                    ByteBuffer buffer = ByteBuffer.allocate(1024);

                    buffer.put(String.format("Current Time: %s, Conn Status: Successful", LocalDateTime.now())
                            .getBytes(StandardCharsets.UTF_8));
                    buffer.flip();
                    socketChannel.write(buffer);

                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    // launch a thread to listen keyboard.
                    executorService.submit(() -> {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                        while (true) {
                            buffer.clear();
                            String sendMsg = reader.readLine();

                            buffer.put(sendMsg.getBytes(StandardCharsets.UTF_8));
                            buffer.flip();
                            socketChannel.write(buffer);
                        }
                    });
                }
                key.interestOps(SelectionKey.OP_READ);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).addJudgement(SelectionKey::isReadable, key -> {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            try {
                while (socketChannel.read(buffer) > 0) ;
                buffer.flip();
                String recvedMsg = String.valueOf(StandardCharsets.UTF_8.decode(buffer));
                System.out.printf("Recved %s from server...\n", recvedMsg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });
        return judgementsChain;
    }

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.connect(new InetSocketAddress(8899));


        JudgementsChain<SelectionKey> judgementsChain = getMyJudgementsChain(selector);

        while (true) {
            // blocks util the event of channel is triggered.
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            for (SelectionKey key : selectionKeys) {
                key.interestOps();
                key.readyOps();
                judgementsChain.run(key);
            }
            selectionKeys.clear();
        }


    }
}
