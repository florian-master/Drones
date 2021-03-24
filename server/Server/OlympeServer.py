import sys
import os
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../../"%(dir_path))
from server.Drone.droneOlympe import DroneOlympe
from server.mark import Mark
from server.DroneServer import DroneServer
from server.location import Location
from server.picture import Picture


import json 
import time

class OlympeServer(DroneServer):

    def __init__(self,ip,port):
        DroneServer.__init__(self,ip,port)
        self.drone = DroneOlympe()

    
        
    
    def takePhoto(self, client,obj):
        try:
            currentLocation= self.drone.getCurrentLocation()
            locations = obj["locations"]
            client.sendConfirmation("picture","see you later")
            for location in locations :    
                latitude = float(location["latitude"])
                longitude = float(location["longitude"])
                altitude = float(location["altitude"])
                photo_path = self.drone.takePhoto(longitude,latitude,altitude)
                print(photo_path)
                client.pics.append(Picture(photo_path,int(time.time())))
            self.drone.flyTo(currentLocation["longitude"], currentLocation["latitude"],currentLocation["altitude"])
        except ValueError as e :
            client.sendError("picture",str(e))
        except KeyError as e :
            client.sendError("picture",str(e))


    def flyTo(self,client, obj):
        if self.userControlling != "" and (client.userId == self.userControlling.userId):
            if self.drone.flying==False:
                client.sendMinusOne("flyTo","not flying")
            else:
                try:
                    latitude = float(obj["latitude"])
                    longitude = float(obj["longitude"])
                    altitude = float(obj["altitude"])

                    client.sendConfirmation("flyTo","")
                    self.drone.flyTo(longitude,latitude,altitude)
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

   

    def flyToWatch(self,client,obj):
        if self.userControlling != "" and (client.userId == self.userControlling.userId):
                try:
                    dstId = obj["dstId"]
                    userIdx = self.clients.index(dstId)
                    dstClient = self.clients[userIdx]
                    location = dstClient.locations[-1]
                    messageToSend = {
                        "op":"flyToWatch",
                        "userId": client.userId,
                        "response": 1
                    }
                    client.sendConfirmation("flyToWatch","")
                    self.drone.flyTo(location.longitude, location.latitude)
                except KeyError:
                    client.sendError("flyToWatch","can't find dstId field")
                except ValueError:
                    
                    client.sendError("flyToWatch","this Id isn't in database")
        else:
            
            client.sendError("flyToWatch","")
    



def main():
    ip = ""
    port = ""
    with open("%s/../server_config.json"%(dir_path)) as json_file:
        data = json.load(json_file)
        ip = data["ip"]
        port = data["port"]

    server = OlympeServer(ip,port)
    server.run()

if __name__ == "__main__":
    main()