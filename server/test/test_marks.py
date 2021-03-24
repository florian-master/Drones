import unittest
import socket
import multiprocessing
import time
import sys
import json
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../../"%(dir_path))
from server.Server.BasicServer import BasicServer

server="localhost"
port = 8083

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

    def test_send_validMark(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr) 
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        clientId = receivedMessage["userId"]
        messageToSend = {
            "op":"mark",
            "longitude":"44.812721",
            "latitude":"-0.590740",
            "userId":clientId,
            "color":"red"
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"], "mark")
        self.assertEqual(receivedMessage["message"], "Position placed")
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_send_invalidMark(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr) 
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        clientId = receivedMessage["userId"]
        messageToSend = {
            "op":"mark",
            "latitude":"-0.590740",
            "userId":clientId,
            "color":"red"
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"], "mark")
        self.assertEqual(receivedMessage["response"], 0)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_getAllMarks(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr) 
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        clientId = receivedMessage["userId"]
        messageToSend = {
            "op":"mark",
            "longitude":"20",
            "latitude":"10",
            "userId":clientId,
            "color":"blue"
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"], "mark")
        self.assertEqual(receivedMessage["response"], 1)
        self.assertEqual(receivedMessage["message"], "Position placed")
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()



        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr) 
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        clientId = receivedMessage["userId"]
        messageToSend = {
            "op":"getMarkedLocations",
            "userId":clientId
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"], "marks")
        self.assertEqual(receivedMessage["marks"][0]["longitude"], 20)
        self.assertEqual(receivedMessage["marks"][0]["latitude"], 10)
        
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
        

    def test_send_multipleMarks(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr) 
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        clientId = receivedMessage["userId"]
        messageToSend = {
            "op":"marks",
            "marks":[
                {
                    "longitude":"20",
                    "latitude":"10",
                    "color": "blue"
                },
                {
                    "longitude":"50",
                    "latitude":"4",
                    "color": "red"
                }
            ],
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(receivedMessage["op"], "marks")
        self.assertEqual(receivedMessage["message"], "Positions placed")
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    
    
if __name__=="__main__":
    unittest.main()