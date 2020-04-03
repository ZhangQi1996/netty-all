#!/usr/bin/python
# -*- coding: utf-8 -*-

__author__ = 'David Zhang'

from thrift import Thrift
from thrift.transport import TSocket, TTransport
from thrift.protocol import TCompactProtocol
from pythrift import PersonService, ttypes


if __name__ == '__main__':

    transport = None

    try:
        t_socket = TSocket.TSocket('localhost', 8899)
        t_socket.setTimeout(600)

        transport = TTransport.TFramedTransport(t_socket)
        protocol = TCompactProtocol.TCompactProtocol(transport)
        client = PersonService.Client(protocol)

        transport.open()

        person = client.getPersonByUsername("David")

        print(person)

        person = ttypes.Person('Phoebe', 18, False)

        client.savePerson(person)
    except Thrift.TException as ex:
        print(ex)
    finally:
        if transport is not None:
            try:
                transport.close()
            except Exception as ex:
                print(ex)
