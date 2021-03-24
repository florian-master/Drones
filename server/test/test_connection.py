import unittest
import socket
import multiprocessing
import time
import sys
import json
import random
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../../"%(dir_path))
from server.Server.BasicServer import BasicServer
uId = ""
client1=""
client2=""
server="localhost"
port = 8080
class TestSuites(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.droneServer = BasicServer(server,port)
        cls.server_thread = multiprocessing.Process(target=cls.droneServer.run)
        cls.server_thread.start()
        time.sleep(0.00001) 
        cls.addr = (server, port)
        
    @classmethod
    def tearDownClass(cls):
        cls.server_thread.terminate()

    def test_connection(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr)
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"],"connect")
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()


    def reconnection(self):
        self.create_client_empty("")
        self.create_client_empty(uId)
        self.assertEqual(1,1)

    def connection_other_client(self):
        self.create_client_empty(uId)
        self.create_client_empty("")
        self.assertEqual(1,1)

    def create_client_empty(self,userId):
        global uId
        addr = (server,port)
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(addr)
        messageToSend = {
            "op":"connexion",
            "userId":userId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"],"connect")
        uId = receivedMessage["userId"]
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
    
if __name__=="__main__":
    unittest.main()