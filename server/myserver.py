import socket
import threading
import json
import uuid
import os
import sys
dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../"%(dir_path))

from server.device import Device
import uuid 

class SocketServer(socket.socket):
    clients = []
    clientsIds = []

    def __init__(self,ip,port):
        socket.socket.__init__(self)
        #To silence- address occupied!!
        self.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.ip = ip
        self.port = port
        self.bind((ip, int(port)))
        self.listen(5)
        self.disconnect_message = "!DISCONNECT"

    def run(self):
        print("Server started")
        print(self.ip)
        try:
            self.accept_clients()
        except Exception as ex:
            print(ex)
        finally:
            print("Server closed")
            for client in self.clients:
                client.close()
            self.close()

    def accept_clients(self):
        while True:
            (clientsocket, address) = self.accept()
            #Adding client to clients list
            #new_person = Person(clientsocket,len(self.clients))
            #self.clients.append(new_person)
            #Client Connected
            #self.onopen(new_person)
            #self.onopen(clientsocket) 
            #Receiving data from client
            thread = threading.Thread(target=self.recieve, args=(clientsocket,))
            thread.start()

    def recieve(self, client):
        device = []
        while 1:
            data = client.recv(1024).decode('utf-8')
            if data == self.disconnect_message or data =='':
                print("deconnexion")
                break

            
            #Temporary reading data to get the userId 
            temp_data = json.loads(data)
            #Checking if client already exist or no
            try:
                #if(temp_data["op"]=="firstConnect"):
                #    userId = uuid.uuid1()
                #    print("Client not existing in database")
                #    newDevice = Device(client,userId)
                #    self.clients.append(newDevice)
                    # Send id generated to the client
                #    message = "connected\n"
                #    client.send(message.encode('utf-8'))
                
                if not(temp_data["userId"] in self.clients):
                    print("Client not existing in database")
                    userId = temp_data["userId"]
                    while(userId in self.clients ):
                        userId = uuid.uuid1()
                    print("userId generated : ",userId)
                    newDevice = Device(client,userId)
                    self.clients.append(newDevice)
                    device = newDevice


                else:
                    print("Client already existing in database")
                    deviceIndex = self.clients.index(temp_data["userId"])
                    existingDevice = self.clients[deviceIndex]
                    existingDevice.client_socket=client
                    #existingDevice.setSocket(client)
                    device = existingDevice  

                #Message Received
                self.onmessage(device, data)
            
            except KeyError as e:
                errorToSend = {
                    "op":"error",
                    "error": str(e)
                }
                client.send(json.dumps(errorToSend).encode('utf-8'))
        #Client Disconnected
        self.onclose(device)
        #Closing connection with client
        client.close()
        #Closing thread

    def broadcast(self, message):
        #Sending message to all clients
        for client in self.clients:
            client.send(message)

    def onopen(self, client):
        pass

    def onmessage(self, client, message):
        pass

    def onclose(self, client):
        pass
