import unittest
import socket
import threading
import time
import sys
import json
import random
import multiprocessing
import os
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

dir_path = os.path.dirname(os.path.realpath(__file__))
test_images_path=""
with open("%s/../server_config.json"%(dir_path)) as json_file:
    data = json.load(json_file) 
    test_images_path = data["test_images_path"]

def printUsage():
    print("test1 : connection / disconnection test")
    print("test2 : taking off / landing test")
    print("test3 : shifting test")
    print("test4 : take photo test")
    print("d: close")   


print("---Welcome to tests scenario---")
printUsage()
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.settimeout(50)
client.connect(("localhost",8080))

messageToSend = {
    "op":"connexion",
    "userId":"",
}
client.send(json.dumps(messageToSend).encode("utf-8"))
receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
print(receivedMessage)

clientId = receivedMessage["userId"]



while 1:
    command = input()
    print(command)
    if(command =="d"):
        break


    elif(command=="test1"):
        print("\n ~~ Connection/Disconnection test ~~\n ")
        messageToSend = {
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        if(receivedMessage["response"]==1):
            print("Connection test passed")
        else:
            print("Connection test failed")

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        if receivedMessage["response"]==1:
            print("Disconnection test passed")
        else:
            print("Disconnection test failed")
        print("\nTest1 ended")


    elif(command=="test2"):
        print("\n ~~ Taking off/Landing test ~~\n ")
        messageToSend = {
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        
        messageToSend = {
            "op":"takeOff",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        if receivedMessage["response"]==1:
            print("Take off test passed")
        else:
            print("Take off test failed")

        time.sleep(3)

        messageToSend = {
            "op":"landing",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        message = client.recv(1024).decode('utf-8')
        receivedMessage = json.loads(message)
        if receivedMessage["response"]==1:
            print("Landing test passed")
        else:
            print("Landing test failed")

        

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        

        print("\nTest2 ended")

    elif(command=="test3"):
        print("\n ~~ Shifting test ~~\n ")
        messageToSend = {
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        
        messageToSend = {
            "op":"takeOff",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        

        time.sleep(3)
        messageToSend = {
            "op":"flyTo",
            "longitude": -0.5974122249979017,
            "latitude": 44.80752672816607,  
            "altitude": 4,
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        message = client.recv(1024).decode('utf-8')
        receivedMessage = json.loads(message)
        print(receivedMessage)
        if receivedMessage["response"]==1:
            print("Shifting test passed")
        else:
            print("Shifting test failed")

        messageToSend = {
            "op":"landing",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        message = client.recv(1024).decode('utf-8')
        receivedMessage = json.loads(message)
        

        

        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        

        print("\nTest3 ended")


    elif(command=="test4"):
        print("\n ~~ Take photo ~~\n ")
        messageToSend = {
            "op":"requestControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        
        messageToSend = {
            "op":"takeOff",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        
        time.sleep(3)

        messageToSend = {
            "op":"takePhoto",
            "locations":[
                {
                    "longitude": -0.5972915127004879,
                    "latitude": 44.80756019,
                    "altitude": 2,
                },
                {
                    "longitude": -0.597164817584725,
                    "latitude": 44.80758543150573, 
                    "altitude": 2,
                }
            ],
            
            "userId": clientId
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        
        if receivedMessage["response"]==1:
            print("Take photo test passed")
        else:
            print("Take photo test failed")

        client.send("!DISCONNECT".encode('utf-8'))

        time.sleep(30)

        client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client.settimeout(50)
        client.connect(("localhost",8080))  
        messageToSend = {
            "op":"getPicture",
            "userId": clientId
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        nbPictures = receivedMessage["nbPictures"]
        client.send("ok".encode("utf-8"))
        for i in range(nbPictures):
            f = open(test_images_path+"/%s.jpg"%(i),"wb")
            print(test_images_path+"/%s.jpg"%(i))
            while True:
                message = client.recv(1024)
                if (not message) or (message ==b"\n") or (message==b"") or (message==b"!end"):
                    break
                else:
                    f.write(message)
            f.close()
        
        messageToSend = {
            "op":"landing",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        message = client.recv(1024).decode('utf-8')
        receivedMessage = json.loads(message)

    
        messageToSend = {
            "op":"giveBackControl",
            "userId":clientId,
        }
        client.send(json.dumps(messageToSend).encode("utf-8"))
        receivedMessage = json.loads(client.recv(1024).decode('utf-8'))
        
        # img = mpimg.imread(test_images_path+"/2.jpg")
        # imgplot = plt.imshow(img)
        # plt.show()
        print("\nTest4 ended")
    else:
        print("typo ? ")
        printUsage()
        

client.send("!DISCONNECT".encode('utf-8'))
client.close()

