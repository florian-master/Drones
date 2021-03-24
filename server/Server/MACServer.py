import sys
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../"%(dir_path))
from server.Drone.droneMAC import DroneExample
from server.mark import Mark
from server.myserver import SocketServer
from server.location import Location


import json 
import time

class BasicDroneServer(SocketServer):

    def __init__(self,ip,port):
        SocketServer.__init__(self,ip,port)
        self.drone = DroneExample()
        self.markedPositions = []
        self.userControlling = ""

    def onmessage(self, client, message):
        print ("Client Sent Message")
        print(message)
        obj = json.loads(message)
        if(obj["op"]=="location"):
            self.addLocation(client,obj)
        if(obj["op"]=="share"):
            client.send("goodbye")
            self.drone.searchOtherWatch()
        if(obj["op"]=="locations"):
            self.addLocations(client,obj)
        if(obj["op"]=="takeOff"):
            if self.userControlling=="" or self.userControlling.userId!=client.userId:
                client.sendError("takeOff", "")
            else:
                self.drone.takeOff(client)
        if(obj["op"]=="landing"):
            if self.userControlling=="" or self.userControlling.userId!=client.userId:
                client.sendError("landing", "")
            else:
                self.drone.landing(client)
        if(obj["op"]=="connexion"):
            messageToSend = {
                "op":"connect",
                "userId": obj["userId"]
            }
            #client.connected=True
            client.userId = obj["userId"]
            client.send((json.dumps(messageToSend)+"\n"))
        if(obj["op"]=="getLocations"):
            self.sendLocations(client)
        
        if(obj["op"]=="flyTo"):
            if self.userControlling != "" and (client.userId == self.userControlling.userId) and self.drone.flying==True:
                try:
                    latitude = float(obj["latitude"])
                    longitude = float(obj["longitude"])
                    self.drone.flyTo(longitude,latitude)
                    client.sendConfirmation("flyTo","")
                except (ValueError, KeyError) as e :
                    client.sendError("flyTo",str(e))
            else:
                client.sendError("flyTo","need control")
        if(obj["op"]=="mark"):
            try:
                latitude = float(obj["latitude"])
                longitude = float(obj["longitude"])
                color = obj["color"]
                self.markPosition(longitude,latitude,color)
                client.sendConfirmation("mark","Position placed")
            except ValueError as e :
                client.sendError("mark",str(e))
            except KeyError as e :
                client.sendError("mark",str(e))
        
        if(obj["op"]=="marks"):
            try:
                for mark in obj["marks"]:
                    latitude = float(mark["latitude"])
                    longitude = float(mark["longitude"])
                    color = mark["color"]
                    self.markPosition(longitude,latitude,color)
                client.sendConfirmation("marks","Positions placed")
            except ValueError as e :
                client.sendError("marks",str(e))
            except KeyError as e :
                client.sendError("marks",str(e))
        
        if(obj["op"]=="getMarkedLocations"):
            try:
                self.sendMarkedPositions(client)
            except ValueError as e :
                client.sendError("getMarkedLocations",str(e))
            except KeyError as e :
                client.sendError("getMarkedLocations",str(e))

        if(obj["op"]=="giveBackControl"):
            if self.userControlling != "" and (client.userId == self.userControlling.userId):
                client.connected=False
                self.userControlling=""
                client.sendConfirmation("giveBackControl","")
            else:
                client.sendError("giveBackControl","")
        if(obj["op"]=="requestControl"):
            if(self.userControlling==""):
                print("Si personne n'est connecté on donne le controle")
                self.userControlling=client
                client.connected=True
                client.sendConfirmation("requestControl","")
                print("User controlling ==")
                print(self.userControlling.userId)
            else:
                if(self.userControlling.userId==client.userId):
                    client.connected=True
                    client.sendConfirmation("requestControl","")
                else:
                    print("Si quelqu'un d'autre est déjà connecté on refuse le controle")
                    client.sendError("requestControl","")

    def onopen(self, client):
        print("Client Connected")
        message = "connected\n"
        client.send(message.encode('utf-8'))

    def onclose(self, client):
        print ("Client Disconnected")
    

    def markPosition(self,longitude,latitude,color):
        self.markedPositions.append(Mark(longitude,latitude,color))
    def sendMarkedPositions(self,client):
        result = {
            "op":"marks",
            "marks":[]
        }
        for mark in self.markedPositions :
            result["marks"].append(mark.__dict__)
        client.send(json.dumps(result) + "\n")


    def sendLocations(self,currClient):
        result = {"op":"locations", "clients":[]}
        for idx, client in enumerate(self.clients):
            clientInfo={}
            if(client == currClient):
                continue
            locations = client.getLocations(currClient.lastUpdate)
            if(locations==[]):
                continue
            clientInfo['locations'] = locations
            clientInfo['userId'] = client.userId 
            result["clients"].append(clientInfo)
       
        currClient.send(json.dumps(result)+"\n")
        currClient.lastUpdate=int(time.time())

    def addLocation(self,client,infos):
        print("[UPDATE LOCATION]: id: "+ str(client.userId)+ " location: " +str(infos))
        try :
            longitude = float(infos["longitude"])
            latitude = float(infos["latitude"])
            milliseconds = float(infos["time"])
             
            if(longitude == 0.0 or latitude==0.0):
                client.sendError("Invalid longitude or latitude")
            else:
                if( milliseconds == 0):
                    milliseconds = int(time.time())
                client.updateLocations(Location(longitude,latitude,milliseconds))
                client.sendConfirmation("location","Location updated")
        except ValueError as e:
            client.sendError("location",e)
        except KeyError as e:
            client.sendError("location",e)

    def addLocations(self,client,infos):
        print("[UPDATE LOCATIONS]: id: "+ str(client.userId)+ " location: " +str(infos))
        try :
            locations = infos["locations"]
            for location in locations:    
                longitude = float(location["longitude"])
                latitude = float(location["latitude"])
                milliseconds = float(location["time"])
                if(longitude == 0.0 or latitude==0.0):
                    continue
                else:
                    if(milliseconds==0):
                        milliseconds = int(time.time())
                    client.updateLocations(Location(longitude,latitude,milliseconds))
            client.sendConfirmation("locations","Locations updated")
        except ValueError as e:
            client.sendError("locations",e)
        except KeyError as e:
            client.sendError("locations",e)

    def getAllInfos(self,currClient):
        result = {"op":"locations", "clients":[]}
        for idx, client in enumerate(self.clients):
            if(client == currClient):
                continue
            clientInfo = client.toJson()
            clientInfo['userId'] = client.userId
            result["clients"].append(clientInfo)
        return result
            
            
def main():
    ip =sys.argv[1]
    port = sys.argv[2]
    server = BasicDroneServer(ip,port)
    server.run()

if __name__ == "__main__":
    main()
