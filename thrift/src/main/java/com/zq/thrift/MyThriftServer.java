package com.zq.thrift;

import com.zq.gen_thrift.*;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

public class MyThriftServer {

    static class PersonServiceHandler implements PersonService.Iface {

        @Override
        public Person getPersonByUsername(String username) throws DataException, TException {

            Person person = new Person();
            person.setUsername(username).setAge(20).setMarried(false);
            return person;
        }

        @Override
        public void savePerson(Person person) throws DataException, TException {
            System.out.println(person);
        }
    }

    public static void main(String[] args) throws Exception {
        TNonblockingServerSocket socket = new TNonblockingServerSocket(8899);
        // HsHa -- half-sync, half-async
        THsHaServer.Args arg = new THsHaServer.Args(socket)
                .minWorkerThreads(2)
                .maxWorkerThreads(4);
        PersonService.Processor<PersonServiceHandler> processor =
                new PersonService.Processor<>(new PersonServiceHandler());

        arg.protocolFactory(new TCompactProtocol.Factory()) // protocol at app-layer
                .transportFactory(new TFramedTransport.Factory()) // transport layer
                .processorFactory(new TProcessorFactory(processor));

        TServer server = new THsHaServer(arg);

        System.out.println("Thrift server started...");

        server.serve(); // endless loop
    }
}
