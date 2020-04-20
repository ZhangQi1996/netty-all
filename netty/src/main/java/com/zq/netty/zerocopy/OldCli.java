package com.zq.netty.zerocopy;

import com.zq.utils.FileUtil;
import com.zq.utils.LogUtil;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class OldCli {

    static final String FROM_FILE_PATH
            = "C:\\Users\\35892\\Desktop\\VirtualBox-6.0.4-128413-Win.zip";

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;

        try {
            socket = new Socket("localhost", 8899);

            fileInputStream = new FileInputStream(FROM_FILE_PATH);
            outputStream = socket.getOutputStream();

            byte[] bytes = new byte[4096];
            int len = 0, total = 0;

            long startTime = System.currentTimeMillis();

            while ((len = fileInputStream.read(bytes)) != -1) {
                total += len;
                outputStream.write(bytes, 0, len);
            }

            LogUtil.LOG.info("the total bytes read are {}, costs {} ms totally.",
                    total, System.currentTimeMillis() - startTime);

        } finally {
            FileUtil.close(fileInputStream);
            FileUtil.close(outputStream);
            FileUtil.close(socket);
        }
        }
    }
