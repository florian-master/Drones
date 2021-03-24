import unittest
import socket
import multiprocessing
import threading
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
port = 8082
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


    def test_send_valid_location(self):
        print("START VALID LOCATION")
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr)
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        client.recv(1024)
        locationToSend = {
            "op": "locations",
            "userId":uId,
            "locations": [
                {
                    "longitude": 12.3,
                    "latitude": 5.10,
                    "time":int(time.time())
                }
            ]
            
        }
        client.send(json.dumps(locationToSend).encode('utf-8'))
        confirmationMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(confirmationMessage["message"],'Locations updated')
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
        print("END VALID LOCATION")

    def test_send_invalid_location(self):
        print("START INVALID LOCATION")
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr)   
        messageToSend = {
            "op":"connexion",
            "userId":"",
        }
        client.send(json.dumps(messageToSend).encode("utf-8")) 
        client.recv(1024)
        locationToSend = {
            "op": "locations",
            "locations": [
                {
                    "longitude": 12.3,
                }
            ]
        }
        client.send(json.dumps(locationToSend).encode('utf-8'))
        errorMessage = json.loads(client.recv(1024).decode('utf-8'))
        self.assertEqual(errorMessage["op"],'error')
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
        print("END INVALID LOCATION")


    def test_getAllLocations(self):
        print("START GETALLLOCATION")
        list_of_locations = []
        for i in range (0,4) :
            new_location = {
                "longitude":random.randint(0,50),
                "latitude": random.randint(0,50),
                "time": int(time.time())
            }
            list_of_locations.append(new_location)
            time.sleep(2)
        self.create_client(list_of_locations)
        time.sleep(2)
    

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
            "op":"getInfos",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        
        locations = json.loads(client.recv(1024).decode("utf-8"))
        print(len(locations["clients"]),1)
        print(len(locations["clients"][0]["locations"]),4)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
        print("END GETALLLOCATION")

    def create_client(self,list_of_locations):
        clientId = ""
        server = "localhost"
        port = 8080
        addr = (server,port)
        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(1)
        client.connect(self.addr)   
        messageToSend = {
            "op":"connexion",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        clientId = receivedMessage["userId"]
        locationToSend = {
            "op": "locations",
            "userId":clientId,
            "locations": list_of_locations
        }
        client.send(json.dumps(locationToSend).encode('utf-8'))
        client.recv(1024)
        client.send("!DISCONNECT".encode('utf-8'))
        client.close()
    
if __name__=="__main__":
    unittest.main()