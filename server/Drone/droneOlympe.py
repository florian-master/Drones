import unittest
import socket
import multiprocessing
import time
import sys
import json
import random
import os
import olympe
import re
import shutil
import xml.etree.ElementTree as ET
import requests

from olympe.messages.gimbal import set_target 

from olympe.messages.ardrone3.Piloting import (
    TakeOff, 
    moveBy, 
    Landing, 
    moveTo, 
    Circle, 
    PCMD
)

from olympe.messages.ardrone3.PilotingState import (
    moveToChanged, 
    FlyingStateChanged, 
    PositionChanged, 
    AttitudeChanged
)

from olympe.messages.ardrone3.GPSSettingsState import GPSFixStateChanged
from olympe.messages.ardrone3.PilotingState import GpsLocationChanged
from olympe.enums.ardrone3.Piloting import MoveTo_Orientation_mode
from olympe.enums.ardrone3.PilotingState import FlyingStateChanged_State

from olympe.messages.camera import (
    set_camera_mode,
    set_photo_mode,
    take_photo,
    photo_progress,
)

dir_path = os.path.dirname(os.path.realpath(__file__))
sys.path.insert(1,"%s/../../"%(dir_path))
from server.Drone.droneClient import DroneExample



ANAFI_IP = "10.202.0.1"
ANAFI_URL = "http://{}/".format(ANAFI_IP)
ANAFI_MEDIA_API_URL = ANAFI_URL + "api/v1/media/medias/"
XMP_TAGS_OF_INTEREST = (
    "CameraRollDegree",
    "CameraPitchDegree",
    "CameraYawDegree",
    "CaptureTsUs",
    # NOTE: GPS metadata is only present if the drone has a GPS fix
    # (i.e. they won't be present indoor)
    "GPSLatitude",
    "GPSLongitude",
    "GPSAltitude",
)
class DroneOlympe(DroneExample):
    def __init__(self):
        super().__init__()
        with open("%s/../server_config.json"%(dir_path)) as json_file:
            data = json.load(json_file)
            self.images_path = data["images_path"]
        self.suppressLogs()
        self.drone = olympe.Drone(ANAFI_IP)
        self.drone.connection()
        self.timeout = 3
        self.flying = False
        self.drone( set_target( gimbal_id = 0,
                                  control_mode = "position",
                                  yaw_frame_of_reference = "relative",
                                  yaw = 0,
                                  pitch_frame_of_reference = "relative",
                                  pitch = -90,
                                  roll_frame_of_reference = "relative",
                                  roll = 0.0
                                ) ).wait()

    def takeOff(self,client):  
        print("Taking off")
        if(self.drone.get_state(FlyingStateChanged)['state']== FlyingStateChanged_State.landed):
            self.drone(
                FlyingStateChanged(state="hovering", _policy="check")
                | FlyingStateChanged(state="flying", _policy="check")
                | (
                    GPSFixStateChanged(fixed=1, _timeout=self.timeout, _policy="check_wait")
                    >> (
                        TakeOff(_no_expect=True)
                        & FlyingStateChanged(
                            state="hovering", _timeout=self.timeout, _policy="check_wait")
                    )
                )
            ).wait()
            messageToSend = {
                "op":"takeOff",
                "userId": client.userId,
                "response": 1
            }
            self.flying=True
        else:
            messageToSend = {
                "op":"takeOff",
                "userId": client.userId,
                "response": 0
            }
        client.send((json.dumps(messageToSend)+"\n"))


    def landing(self,client): 
        print("Landing")
        self.drone(
            Landing()
            >> FlyingStateChanged(state="landed", _timeout=self.timeout)
        ).wait()
        messageToSend = {
            "op":"landing",
            "userId": client.userId,
            "response": 1
        }
        self.flying=True
        client.send((json.dumps(messageToSend)+"\n"))

    def flyTo(self,longitude,latitude,altitude=-1):
        drone_location = self.drone.get_state(GpsLocationChanged)
        if(altitude==-1):
            altitude = drone_location["altitude"]
        if(longitude==0.0 and latitude ==0.0):
            longitude = drone_location["longitude"]
            latitude = drone_location["latitude"]
        if(self.drone.get_state(FlyingStateChanged)['state']!= FlyingStateChanged_State.landed):
            self.drone(
                moveTo(latitude,  longitude, altitude, MoveTo_Orientation_mode.TO_TARGET, 0.0)
                >> FlyingStateChanged(state="hovering", _timeout=self.timeout)
                >> moveToChanged(latitude=latitude, longitude=longitude, altitude=2, orientation_mode=MoveTo_Orientation_mode.TO_TARGET, status='DONE', _policy='wait')
                >> FlyingStateChanged(state="hovering", _timeout=self.timeout) 
            ).wait()
            return True
        else:
            return False
            
        
    def takePhoto(self,longitude,latitude,altitude=-1):
        drone_location = self.drone.get_state(GpsLocationChanged)
        self.setup_photo_mode()
        print(drone_location)
        self.flyTo(longitude,latitude,altitude)
        # take a photo burst and get the associated media_id
        photo_saved = self.drone(photo_progress(result="photo_saved", _policy="wait"))
        self.drone(take_photo(cam_id=0)).wait()
        photo_saved.wait()
        media_id = photo_saved.received_events().last().args["media_id"]
        print(media_id)
        # download the photos associated with this media id
        media_info_response = requests.get(ANAFI_MEDIA_API_URL + media_id)
        media_info_response.raise_for_status()
        download_dir = self.images_path
        for resource in media_info_response.json()["resources"]:
            image_response = requests.get(ANAFI_URL + resource["url"], stream=True)
            download_path = os.path.join(download_dir, resource["resource_id"])
            print(download_path)
            image_response.raise_for_status()
            with open(download_path, "wb") as image_file:
                shutil.copyfileobj(image_response.raw, image_file)

            # parse the xmp metadata
            with open(download_path, "rb") as image_file:
                image_data = image_file.read()
                image_xmp_start = image_data.find(b"<x:xmpmeta")
                image_xmp_end = image_data.find(b"</x:xmpmeta")
                image_xmp = ET.fromstring(image_data[image_xmp_start : image_xmp_end + 12])
                for image_meta in image_xmp[0][0]:
                    xmp_tag = re.sub(r"{[^}]*}", "", image_meta.tag)
                    xmp_value = image_meta.text
                    # only print the XMP tags we are interested in
                    if xmp_tag in XMP_TAGS_OF_INTEREST:
                        print(resource["resource_id"], xmp_tag, xmp_value)
        return download_path

    def setup_photo_mode(self):
        self.drone(set_camera_mode(cam_id=0, value="photo")).wait()
        # For the file_format: jpeg is the only available option
        self.drone(
            set_photo_mode(
                cam_id=0,
                mode="single",
                format="rectilinear",
                file_format="jpeg",
                burst="burst_14_over_1s",
                bracketing="preset_1ev",
                capture_interval=0.0,
            )
        ).wait()

        


    def suppressLogs(self):
        olympe.log.update_config({
            "handlers": {
                "olympe_log_file": {
                    "class": "logging.FileHandler",
                    "formatter": "default_formatter",
                    "filename": "olympe.log"
                },
                "ulog_log_file": {
                    "class": "logging.FileHandler",
                    "formatter": "default_formatter",
                    "filename": "ulog.log"
                },
            },
            "loggers": {
                "olympe": {
                    "handlers": ["olympe_log_file"]
                },
                "ulog": {
                    "level": "DEBUG",
                    "handlers": ["ulog_log_file"],
                }
            }
        })

    def getCurrentLocation(self):
        drone_location = self.drone.get_state(GpsLocationChanged)
        print(drone_location)
        return {
            "longitude": drone_location["longitude"],
            "latitude": drone_location["latitude"],
            "altitude": drone_location["altitude"]
        }