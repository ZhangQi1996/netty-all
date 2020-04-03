package com.zq.thrift;

import com.zq.gen_thrift.*;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TSocket;

public class MyThriftCli {
    public static void main(String[] args) throws Exception {
        TFastFramedTransport transport =
                new TFastFramedTransport(new TSocket("localhost", 8899), 600);
        TCompactProtocol protocol = new TCompactProtocol(transport);
        PersonService.Client client = new PersonService.Client(protocol);

        try {
            transport.open();

            Person person = client.getPersonByUsername("David");

            System.out.println(person);

            client.savePerson(new Person().setUsername("Phoebe").setAge(18).setMarried(false));
        } finally {
            transport.close();
        }

    }
}
