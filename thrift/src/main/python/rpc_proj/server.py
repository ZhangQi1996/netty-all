#!/usr/bin/python
# -*- coding: utf-8 -*-

__author__ = 'David Zhang'

from thrift import Thrift
from thrift.transport import TSocket, TTransport
from thrift.protocol import TCompactProtocol
from thrift.server import TServer
from pythrift import PersonService, ttypes


class PersonServiceHandler(PersonService.Iface):

    def getPersonByUsername(self, username):
        return ttypes.Person(username, 20, False)

    def savePerson(self, person):
        print(person)


if __name__ == '__main__':
    try:
        TServer.TSimpleServer(
            PersonService.Processor(PersonServiceHandler()),
            # 注意py3的socket tcp通信默认采用tcpv6
            TSocket.TServerSocket(host='127.0.0.1', port=8899),
            TTransport.TFramedTransportFactory(),
            TCompactProtocol.TCompactProtocolFactory()
        ).serve()
    except Thrift.TException as ex:
        print(ex)

