### netty学习
* 子模块
    1. protobuf模块（基于proto2）
        * 模块功能
            * 用来管理基于proto2的.proto文件以及自动生成的对应java源文件
        * 使用protobuf插件来完成.proto文件自动编译为java源文件
            * 插件详见https://github.com/google/protobuf-gradle-plugin
        * 命令
            1. **注意:**使用gradle build命令会自动调用protobuf的插件的generateProto task
               故不想使用generateProto task，使用
               `./gradlew :protobuf:clean :protobuf:build -x generateProto`命令
            2. 使用`./gradlew :protobuf:clean :protobuf:generateProto`完成代码生成
            3. 使用`./gradlew :protobuf:clean :protobuf:build`完成代码生成+子模块构建
    2. grpc模块（基于proto3）
        * 模块功能
            * 用来管理基于proto3的.proto文件以及自动生成的对应java源文件
        * 使用protobuf插件以及内嵌的grpc插件来完成.proto文件自动编译为java源文件
            * grpc-java详见https://github.com/grpc/grpc-java
        * 命令
            1. **注意:**使用gradle build命令会自动调用protobuf的插件的generateProto task
               故不想使用generateProto task，使用
               `./gradlew :grpc:clean :grpc:build -x generateProto`命令
            2. 使用`./gradlew :grpc:clean :grpc:generateProto`完成代码生成
            3. 使用`./gradlew :grpc:clean :grpc:build`完成代码生成+子模块构建
    3. thrift模块
        * 没有引入thrift的gradle插件完成.thrift的java源文件生成
        * 用来管理.thrift文件以及对应java源文件
        * 相关包
            1. com.zq.gen-thrift
                * 存放.thrift文件对应生成的java源文件
            2. com.zq.thrift
                * 基于thrift的C-S rpc代码
        * 相关资源
            1. src/main/python/rpc_proj
                * thrift C-S rpc的python实现
    4. netty模块
        * 引入了protobuf与grpc子模块，实现了相关功能
        * 相关包
            1. com.zq.jdk8
                * 用于相关jdk8新特性使用的相关代码
            2. com.zq.log
                * 用于全局的日志工具
            3. com.zq.netty
                1. .firstexample
                    * 一个使用netty实现的http服务器简单响应
                2. .secondexample
                    * 一个使用netty实现的socket的简单C-S
                3. .thirdexample
                    * 一个使用netty实现的socket的简单C-S（升级一丢丢，完成简陋聊天）
                4. .fourthexample
                    * 基于netty的socket简单心跳机制模拟
                5. .fifthexample
                    * 基于netty的websocket简单实现（搭配resource/test.html）
                6. .sixthexample
                    * 基于protobuf+netty的简单数据传输（没有rpc）
                7. .grpc
                    * 基于grpc+netty的简单rpc
* 整个项目
    * 所有构建可以通过`./gradlew clean build`完成