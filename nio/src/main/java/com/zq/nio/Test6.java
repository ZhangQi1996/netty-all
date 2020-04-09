package com.zq.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;

public class Test6 {
    // Buffer的Scattering与Gathering
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8899));

        int msgLen = 2 + 3 + 4;

        ByteBuffer[] buffers = {
                ByteBuffer.allocate(2),
                ByteBuffer.allocate(3),
                ByteBuffer.allocate(4),
        };

        List<ByteBuffer> bufferList = Arrays.asList(buffers);

        SocketChannel socketChannel = serverSocketChannel.accept();


        while (true) {

            int l = msgLen;

            // read from cli
            while ((l -= socketChannel.read(buffers)) > 0) {
                bufferList.forEach(buf -> {
                    System.out.println(String.format(
                            "pos: %s, limit: %s\n",
                            buf.position(),
                            buf.limit()
                    ));
                });
            }

            l = msgLen;
            // read -> write
            bufferList.forEach(Buffer::flip);
            // write into cli
            while ((l -= socketChannel.write(buffers)) > 0);

            bufferList.forEach(Buffer::clear);
        }
    }
}
