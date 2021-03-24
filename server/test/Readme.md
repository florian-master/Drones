# Tests

## Test client-server communication 

### Prerequies
You need to install unittest module


### Instructions
You can't use the command ``python3 unittest`` to start all tests together


### How to tests
To test the connection between client and server:
``` 
python3 path/to/file/test_connection.py
``` 
To test sending and receiving locations:
``` 
python3 path/to/file/test_locations.py
``` 
To test sending and receiving marks:
``` 
python3 path/to/file/test_marks.py
``` 
To test drone controls:
``` 
python3 path/to/file/test_drone_controle.py
``` 
To test the connection between client and server:
``` 
python3 path/to/file/test_connection.py
``` 
___
## Test client-server-drone communication

### Prerequies
You need to install Olympe and Sphinx ( see [Olympe documentation](https://developer.parrot.com/docs/olympe/installation.html) )

### Instructions
1. Open 3 terminals ( A,B C)
2. Terminal A:  Start sphinx ->  ``sphinx path/to/file/test1.world /opt/parrot-sphinx/usr/share/sphinx/drones/anafi4k.drone::stolen_interface=::simple_front_cam=true ``

3. Terminal B: Start olympe shell 

4. Terminal B: Start server -> ``python3 path/to/file/OlympeServer.py``

5. Terminal C: Start testing client -> ``python3 path/to/file/test_client_drone.py ``

You must see :
```
---Welcome to tests scenario---
test1 : connection / disconnection test
test2 : taking off / landing test
test3 : shifting test
d: close

```

### How to tests
Test1:

To test the conection and disconnection with the drone 

type `test1` in Terminal C, you must see:
```
 ~~ Connection/Disconnection test ~~
 
Connection test passed
Disconnection test passed

Test1 ended
```

Test2:

To test the taking off and the landing 

type `test2` in Terminal C, you must see in the terminal :
```
 ~~ Taking off/Landing test ~~
 
Take off test passed
Landing test passed

Test2 ended
```
And the drone taking off and landing in Spinx window


Test3:

To test the flyTo command

type `test3` in Terminal C, you must see in the terminal :
```
 ~~ Shifting test ~~
 
Shifting test passed

Test3 ended
```
And the drone moving in Spinx window


