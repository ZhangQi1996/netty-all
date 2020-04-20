package com.zq.netty.grpc;

import com.zq.grpc.*;
import com.zq.utils.LogUtil;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {

    // one req, one rsp
    @Override
    public void getRealNameByUsername(MyRequest request, StreamObserver<MyResponse> responseObserver) {
        LogUtil.LOG.info("接收到客户端信息：" + request.getUsername());

        responseObserver.onNext(MyResponse.newBuilder()
                .setRealName("David").build());
        responseObserver.onCompleted();

    }

    // one req, stream rsp
    @Override
    public void getStudentsByAge(StudentAgeRequest request, StreamObserver<StudentResponse> responseObserver) {
        LogUtil.LOG.info("接收到客户端信息：" + request.getAge());

        responseObserver.onNext(StudentResponse.newBuilder()
                .setName("张三").setAge(20).setCity("北京").build());
        responseObserver.onNext(StudentResponse.newBuilder()
                .setName("李四").setAge(21).setCity("上海").build());
        responseObserver.onNext(StudentResponse.newBuilder()
                .setName("王五").setAge(29).setCity("广州").build());
        responseObserver.onNext(StudentResponse.newBuilder()
                .setName("赵六").setAge(18).setCity("深圳").build());

        responseObserver.onCompleted();
    }

    // stream req, one rsp
    @Override
    public StreamObserver<StudentAgeRequest> getStudentsWrapperByAges(StreamObserver<StudentResponseList> responseObserver) {

        return new StreamObserver<StudentAgeRequest>() {
            // 由于请求是流式地，故每当服务器端接收到一个req，就会调用一次onNext方法
            @Override
            public void onNext(StudentAgeRequest value) {
                LogUtil.LOG.info("{}", value.getAge());
            }

            @Override
            public void onError(Throwable t) {

                LogUtil.LOG.error("Error", t);
            }

            // 当所有流式req都结束的时候调用
            @Override
            public void onCompleted() {
                List<StudentResponse> list = Arrays.asList(
                        StudentResponse.newBuilder()
                                .setName("David").setAge(20).setCity("BJ").build(),
                        StudentResponse.newBuilder()
                                .setName("Phoebe").setAge(19).setCity("NY").build()
                );

                responseObserver.onNext(StudentResponseList.newBuilder()
                        .addAllStudentResponse(list).build());
                responseObserver.onCompleted();
            }
        };
    }

    // stream req, stream rsp
    @Override
    public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> responseObserver) {
        return new StreamObserver<StreamRequest>() {
            @Override
            public void onNext(StreamRequest value) {
                LogUtil.LOG.info(value.getRequestInfo());

                responseObserver.onNext(StreamResponse.newBuilder()
                        .setResponseInfo(UUID.randomUUID().toString())
                        .build());
            }

            @Override
            public void onError(Throwable t) {
                LogUtil.LOG.error("Error", t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
