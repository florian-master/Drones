import unittest
import socket
import threading
import time
import sys
import json
import random
import multiprocessing
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../../"%(dir_path))
from server.Server.BasicServer import BasicServer
uId = ""
client1=""
client2=""

server="localhost"
port = 8081
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

    def test_send_requestControl_then_giveBackControl(self):
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
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_send_requestControl_but_another_controlling(self):
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
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]

        messageToSend = {
            "op":"requestControl",
            "userId":"ezoihfzoihfozqrhvr",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 0)

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_send_giveBackControl_without_controlling(self):
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
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 0)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_send_control_takeoff_landing_givecontrolback(self):
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
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]

        messageToSend = {
            "op":"takeOff",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"landing",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    
    def test_takeoff_without_controlling(self):
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
            "op":"takeOff",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 0)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_landing_without_controlling(self):
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
            "op":"landing",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 0)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_flyTo(self):
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
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"takeOff",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"flyTo",
            "userId":clientId,
            "longitude": 58.347987,
            "latitude": -36.735281
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"landing",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_flyTo_without_controlling(self):
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
            "op":"flyTo",
            "userId":clientId,
            "longitude": 58.347987,
            "latitude": -36.735281
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 0)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()

    def test_flyTo_without_flying(self):
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
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)

        messageToSend = {
            "op":"flyTo",
            "userId":clientId,
            "longitude": 58.347987,
            "latitude": -36.735281
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, -1)

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        response = receivedMessage["response"]
        self.assertEqual(response, 1)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
    
if __name__=="__main__":
    unittest.main()