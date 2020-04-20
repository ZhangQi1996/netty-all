package com.zq.netty.grpc;


import com.zq.utils.LogUtil;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(8899)
                .addService(new StudentServiceImpl()).build().start();

        LogUtil.LOG.info("server started...");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LogUtil.LOG.info("shutdown jvm...");
            stop();
        }));
    }

    private void stop() {
        if (null != server) {
            server.shutdown();
        }
    }

    private void awaitTermination() throws InterruptedException {
        if (null != server) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GrpcServer server = new GrpcServer();

        server.start();
        server.awaitTermination();
    }
}
