import json
import time
class Device: 
    def __init__(self, client_socket,userId ):
        self.client_socket = client_socket
        self.locations = []
        self.format = 'utf-8'
        self.userId = userId
        self.lastUpdate = 0
        self.connected = True
        self.pics = []

    def __eq__(self, value):
        return self.userId==value

    def send(self,message):
        try:
            self.client_socket.send(message.encode(self.format))
        except ConnectionResetError:
            print("error")
        except BrokenPipeError:
            print("error")


    def sendPhoto(self,photo_path):
        try:
            f = open(photo_path,"rb")
            l = f.read(1024)
            while l:
                self.client_socket.send(l)
                l = f.read(1024)
            time.sleep(1)
            self.client_socket.send("!end".encode())
            
        except ConnectionResetError:
            print("error")
        except BrokenPipeError:
            print("error")
    def updateLocations(self, location):
        self.locations.append(location)


    def close(self):
        self.client_socket.close()

    def recv(self,buffer):
        return self.client_socket.recv(buffer)

    
    def sendError(self, op, errorMessage):
        messageToSend = {
            "op":op,
            "response":0,
            "message":errorMessage,
            "userId": self.userId
        }
        print("Send Error")
        print(messageToSend)
        self.client_socket.send((json.dumps(messageToSend)+"\n").encode(self.format)) 

    
    def toJson(self):
        res = {"locations":[]}

        for location in self.locations:
            res["locations"].append({
                "longitude": location.longitude,
                "latitude": location.latitude,
                "time": location.seconds
            })
        return res

    def getAllInfos(self,seconds):
        res = {"locations":[]}

        for location in self.getLocations(seconds):
            res["locations"].append({
                "longitude": location["longitude"],
                "latitude": location["latitude"],
                "time": location['seconds']
            })

        return res

    def getPics(self,seconds):
        result = []
        for pic in self.pics:
            if(pic.taken_at > seconds):
                result.append(pic.__dict__)
        return list(result)

    def getLocations(self,seconds):
        result = []
        for loc in self.locations:
            if(loc.seconds > seconds):
                result.append(loc.__dict__)
        return list(result)

    def sendConfirmation(self,op, message):
        messageToSend = {
            "op": op,
            "response":1,
            "message": message,
            "userId": self.userId
        }
        print("Send Confirmation")
        print(messageToSend)
        self.client_socket.send((json.dumps(messageToSend)+"\n").encode(self.format))

    def sendMinusOne(self,op, message):
        messageToSend = {
            "op": op,
            "response":-1,
            "message": message,
            "userId": self.userId
        }
        print("Send Minus One")
        print(messageToSend)
        self.client_socket.send((json.dumps(messageToSend)+"\n").encode(self.format))


    def setSocket(self,socket):
        self.client_socket=socket
    
    def addPic(self, picture):
        self.pics.append(picture)