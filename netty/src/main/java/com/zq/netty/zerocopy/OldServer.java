package com.zq.netty.zerocopy;

import com.zq.utils.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

// 用来测试传统的拷贝的耗时,单连接请求，2.7s左右
public class OldServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream inputStream = null;

        try {
            serverSocket = new ServerSocket(8899);

            while (true) {

                socket = serverSocket.accept();
                inputStream = socket.getInputStream();

                byte[] bytes = new byte[4096];

                while (inputStream.read(bytes) != -1) ;
            }
        } finally {
            FileUtil.close(inputStream);
            FileUtil.close(socket);
            FileUtil.close(serverSocket);
        }

    }
}
