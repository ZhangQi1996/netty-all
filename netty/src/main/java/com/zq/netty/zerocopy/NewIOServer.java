package com.zq.netty.zerocopy;

import com.zq.utils.FileUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NewIOServer {


    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = null;
        SocketChannel socketChannel = null;

        try {
            serverSocketChannel = ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            // 表示当服务器的一个端口处于四次挥手后的time wait时间，默认这个端口仍然是被占用的
            // 当设置为真时，则这个时候其他的请求到来，则新的请求可以绑定到这个端口上。
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(8899));

            ByteBuffer byteBuffer = ByteBuffer.allocate(4096);

            while (true) {
                socketChannel = serverSocketChannel.accept();
//            socketChannel.configureBlocking(true)
                while (socketChannel.read(byteBuffer) != -1) {
                    byteBuffer.rewind();
                }
            }
        } finally {
            FileUtil.close(socketChannel);
            FileUtil.close(serverSocketChannel);
        }
    }
}
