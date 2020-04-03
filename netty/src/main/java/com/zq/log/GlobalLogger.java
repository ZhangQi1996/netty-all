package com.zq.log;

import com.zq.netty.grpc.GrpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalLogger {

    public static final Logger LOG = LoggerFactory.getLogger(GrpcServer.class);

    private GlobalLogger() {}
}
