import socket
import json
#import olympe
import time
#from olympe.messages.ardrone3.Piloting import TakeOff, Landing

DRONE_IP = "10.202.0.1"

class DroneExample : 
    def __init__(self):
        self.flying=False
        #self.drone = olympe.Drone(DRONE_IP)
        
    def searchOtherWatch(self):
        print("The drone is searching for new watch")
        pass

    def takeOff(self,client):  
        print("Taking off")
        if(not self.flying):
            self.flying=True
            #self.drone.connect()
            #assert self.drone(TakeOff()).wait().success()
            #self.drone.disconnect()
            messageToSend = {
                "op":"takeOff",
                "userId": client.userId,
                "response": 1
            }
        else:
            messageToSend = {
                "op":"takeOff",
                "userId": client.userId,
                "response": 0
            }
        client.send((json.dumps(messageToSend)+"\n"))


    def landing(self,client): 
        print("Landing")
        if(self.flying):
            self.flying=False
            #self.drone.connect()
            #assert self.drone(Landing()).wait().success()
            #self.drone.disconnect()
            messageToSend = {
                "op":"landing",
                "userId": client.userId,
                "response": 1
            }
        else:
            messageToSend = {
                "op":"landing",
                "userId": client.userId,
                "response": 0
            }
        client.send((json.dumps(messageToSend)+"\n"))

    def flyTo(self, longitude,latitude):
        print("Flying to (%s,%s)"% (longitude,latitude))
        pass

    def takePhoto(self, longitude,latitude):
        print("Flying to (%s,%s) to take photo"% (longitude,latitude))
        pass

