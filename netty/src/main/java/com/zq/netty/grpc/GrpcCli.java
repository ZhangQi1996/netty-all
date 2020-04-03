package com.zq.netty.grpc;

import com.zq.grpc.*;
import com.zq.log.GlobalLogger;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GrpcCli {

    private static final Object asyncLock = new Object();

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8899)
                .usePlaintext().build();

        StudentServiceGrpc.StudentServiceBlockingStub stub
                = StudentServiceGrpc.newBlockingStub(channel);

        // 单req，单rsp

        MyResponse response = stub.getRealNameByUsername(MyRequest.newBuilder()
                .setUsername("Phoebe").build());

        GlobalLogger.LOG.info(response.getRealName());

        GlobalLogger.LOG.info("-------------------------");

        // 单req，stream rsp

        Iterator<StudentResponse> iterator = stub.getStudentsByAge(StudentAgeRequest.newBuilder()
                .setAge(20).build());

        iterator.forEachRemaining(it -> {
            GlobalLogger.LOG.info("cli has recved: {}", it);
        });

        GlobalLogger.LOG.info("-------------------------");


        // stream req，单rsp
        // 注意对于所有输入类型为stream式的，cli端都必须使用异步的stub
        StudentServiceGrpc.StudentServiceStub asyncStub = StudentServiceGrpc.newStub(channel);
        StreamObserver<StudentAgeRequest> wrapperByAges = asyncStub.getStudentsWrapperByAges(new StreamObserver<StudentResponseList>() {
            @Override
            public void onNext(StudentResponseList value) {
                value.getStudentResponseList().forEach(it -> {
                    GlobalLogger.LOG.info("{}", it);
                });
            }

            @Override
            public void onError(Throwable t) {
                GlobalLogger.LOG.info("Error", t);
            }

            @Override
            public void onCompleted() {
                GlobalLogger.LOG.info("completed..");
                synchronized (asyncLock) {
                    asyncLock.notifyAll();
                }
            }
        });

        new Random().ints(3, 18, 30).forEach(i -> {
            wrapperByAges.onNext(StudentAgeRequest.newBuilder().setAge(i).build());
        });
        wrapperByAges.onCompleted();

        // 由于是异步请求，故之执行完wrapperByAges.onNext后不会等待
        synchronized (asyncLock) {
            asyncLock.wait(TimeUnit.SECONDS.toMillis(5));
        }

        GlobalLogger.LOG.info("------------------------------");

        // stream req, stream rsp
        StreamObserver<StreamRequest> biTalk = asyncStub.biTalk(new StreamObserver<StreamResponse>() {
            @Override
            public void onNext(StreamResponse value) {
                GlobalLogger.LOG.info(value.getResponseInfo());
            }

            @Override
            public void onError(Throwable t) {
                GlobalLogger.LOG.info("Error", t);
            }

            @Override
            public void onCompleted() {
                GlobalLogger.LOG.info("completed..");
                synchronized (asyncLock) {
                    asyncLock.notifyAll();
                }
            }
        });

        Stream.generate(UUID.randomUUID()::toString).limit(10).forEach(str -> {
            biTalk.onNext(StreamRequest.newBuilder().setRequestInfo(str).build());
        });

        biTalk.onCompleted();
        // 由于是异步请求，故之执行完wrapperByAges.onNext后不会等待
        synchronized (asyncLock) {
            asyncLock.wait(TimeUnit.SECONDS.toMillis(10));
        }

    }
}
