import sys
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../../"%(dir_path))
from server.Drone.droneClient import DroneExample
from server.mark import Mark
from server.DroneServer import DroneServer
from server.location import Location


import json 
import time

class BasicServer(DroneServer):

    def __init__(self,ip,port):
        DroneServer.__init__(self,ip,port)
        self.drone = DroneExample()


    def onopen(self, client):
        print("Client Connected")
        message = "connected\n"
        client.send(message.encode('utf-8'))

    def onclose(self, client):
        print ("Client Disconnected")
    



def main():
    ip = ""
    port = ""
    with open("%s/../server_config.json"%(dir_path)) as json_file:
            data = json.load(json_file)
            ip = data["ip"]
            port = data["port"]
        
    server = BasicServer(ip,port)
    server.run()


if __name__ == "__main__":
    main()