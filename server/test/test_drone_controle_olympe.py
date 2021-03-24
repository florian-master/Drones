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
from server.SebServer import BasicDroneServer
uId = ""
client1=""
client2=""

class TestSuites(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.droneServer = BasicDroneServer("localhost",8080)
        cls.server_thread = multiprocessing.Process(target=cls.droneServer.run)
        cls.server_thread.start()
        time.sleep(0.00001) 
        cls.addr = ("localhost", 8080)
        
    @classmethod
    def tearDownClass(cls):
        cls.server_thread.terminate()

    def test_send_control_takeoff_landing_givecontrolback(self):
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1000)
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

if __name__=="__main__":
    unittest.main()