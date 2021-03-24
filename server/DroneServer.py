import sys
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../"%(dir_path))
from server.Drone.droneClient import DroneExample
from server.mark import Mark
from server.iserver import SocketServer
from server.location import Location


import json 
import time

class DroneServer(SocketServer):

    def __init__(self,ip,port):
        SocketServer.__init__(self,ip,port)
        self.drone = DroneExample()
        self.markedPositions = []
        self.userControlling = ""
        self.waiting = False

    def onmessage(self, client, message):
        print ("Client Sent Message")
        print(message)

        obj = json.loads(message)
        if(obj["op"]=="location"):
            self.location(client,obj)
        if(obj["op"]=="share"):
            self.share(client)
        if(obj["op"]=="locations"):
            self.locations(client,obj)
        if(obj["op"]=="takeOff"):
            self.takeOff(client)
        if(obj["op"]=="landing"):
            self.landing(client)
        if(obj["op"]=="connexion"):
            self.connection(client)
        if(obj["op"]=="getLocations"):
            self.getLocations(client)
        if(obj["op"]=="getInfos"):
            self.getInfos(client)
        if(obj["op"]=="flyTo"):
            self.flyTo(client,obj)
        if(obj["op"]=="getPicture"):
            self.getPicture(client)
        if(obj["op"]=="mark"):
            self.mark(client,obj)
        if(obj["op"]=="marks"):
            self.marks(client,obj)
        if(obj["op"]=="getMarkedLocations"):
            self.getMarkedLocations(client)
        if(obj["op"]=="giveBackControl"):
            self.giveBackControl(client)
        if(obj["op"]=="requestControl"):
            self.requestControl(client)
        if(obj["op"]== "takePhoto"):
            self.takePhoto(client,obj)
        if(obj["op"]=="refuseDisconnectionRequest"):
            self.refuseDisconnectionRequest()

    def connection(self,client):
        messageToSend = {
            "op":"connect",
            "userId": client.userId
        }
        #client.connected=True
        print("connection")
        client.send((json.dumps(messageToSend)+"\n"))
    
    
    def refuseDisconnectionRequest(self):
        self.waiting=False

    def takeOff(self,client):
        if self.userControlling=="" or self.userControlling.userId!=client.userId:
            client.sendError("takeOff", "")
        else:
            self.drone.takeOff(client)
        
    def landing(self,client):
        if self.userControlling=="" or self.userControlling.userId!=client.userId:       
            client.sendError("landing", "")
        else:
            self.drone.landing(client)

    def requestControl(self,client):
        # Si personne n'est connecté on donne le controle
        if(self.userControlling==""):
            self.waiting = False
            self.userControlling=client
            client.connected=True
            client.sendConfirmation("requestControl","")
        # Si quelqu'un est déjà connecté on refuse le controle
        else:
            self.waiting=True
            client.sendError("requestControl","")
    
    def giveBackControl(self,client):
        if self.userControlling != "" and (client.userId == self.userControlling.userId):
            client.connected=False
            self.userControlling=""
            client.sendConfirmation("giveBackControl","")
        else:
            client.sendError("giveBackControl","")

    
    def takePhoto(self, client,obj):
        try:
            latitude = float(obj["latitude"])
            longitude = float(obj["longitude"])
            client.sendConfirmation("picture","see you later")
            self.drone.takePhoto(longitude,latitude)
        except ValueError as e :
            client.sendError("picture",str(e))
        except KeyError as e :
            client.sendError("picture",str(e))

    def getMarkedLocations(self,client):
        try:
            result = {
                "op":"marks",
                "marks":[]
            }
            for mark in self.markedPositions :
                result["marks"].append(mark.__dict__)
            client.send(json.dumps(result) + "\n")
        except ValueError as e :
            client.sendError("getMarkedLocations",str(e))
        except KeyError as e :
            client.sendError("getMarkedLocations",str(e))
    
    def marks(self,client,obj):
        try:
            for mark in obj["marks"]:
                latitude = float(mark["latitude"])
                longitude = float(mark["longitude"])
                color = mark["color"]
                self.markedPositions.append(Mark(longitude,latitude,color))
            client.sendConfirmation("marks","Positions placed")
        except ValueError as e :
            client.sendError("marks",str(e))
        except KeyError as e :
            client.sendError("marks",str(e))

    def mark(self,client,obj):
        try:
            latitude = float(obj["latitude"])
            longitude = float(obj["longitude"])
            color = obj["color"]
            self.markedPositions.append(Mark(longitude,latitude,color))
            client.sendConfirmation("mark","Position placed")
        except ValueError as e :
            client.sendError("mark",str(e))
        except KeyError as e :
            client.sendError("mark",str(e))

    def flyTo(self,client,obj):
        if self.userControlling != "" and (client.userId == self.userControlling.userId):
            if self.drone.flying==False:
                client.sendMinusOne("flyTo","not flying")
            else:
                try:
                    latitude = float(obj["latitude"])
                    longitude = float(obj["longitude"])
                    client.sendConfirmation("flyTo","")
                    self.drone.flyTo(longitude,latitude)
                except (ValueError, KeyError) as e :
                    client.sendError("flyTo",str(e))
        else:
            client.sendError("flyTo", "")

    def share(self, client):
        client.send("goodbye")
        self.drone.searchOtherWatch()

    def onopen(self, client):
        print("Client Connected")
        message = "connected\n"
        client.send(message.encode('utf-8'))

    def onclose(self, client):
        if self.userControlling !="" and self.userControlling.userId==client.userId:
            self.userControlling=""
        print ("Client Disconnected")
    

   

    def getLocations(self,currClient):
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

    def location(self,client,infos):
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

    def locations(self,client,infos):
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

    def getPicture(self, currClient):
    
        pics = currClient.getPics(currClient.lastUpdate)
        if(len(pics) >0):
            messageToSend={
                "op":"getPicture",
                "nbPictures": len(pics)
            }
            currClient.send(json.dumps(messageToSend)+"\n")
            currClient.recv(1024)
            for pic in pics:
                currClient.sendPhoto(pic['pic'])
                time.sleep(1)
        else:
            currClient.sendError("getPicture","No images") 

    def getInfos(self,currClient):
        result = {"op":"getInfos", "clients":[],"marks":[],"waiting":0, "connected":0}
        
        for idx, client in enumerate(self.clients):
            
            clientInfo = client.getAllInfos(currClient.lastUpdate)
            clientInfo['userId'] = client.userId
            result["clients"].append(clientInfo)

        for mark in self.markedPositions :
            result["marks"].append(mark.__dict__)

        if(self.waiting==True):
            result['waiting']=1
        if(self.userControlling!=""):
            result['connected']=1
        
        currClient.send(json.dumps(result)+"\n")

        currClient.lastUpdate=int(time.time())
def main():
    ip = ""
    port = ""
    with open("%s/server_config.json"%(dir_path)) as json_file:
        data = json.load(json_file)
        ip = data["ip"]
        port = data["port"]
    server = DroneServer(ip,port)
    server.run()

if __name__ == "__main__":
    main()