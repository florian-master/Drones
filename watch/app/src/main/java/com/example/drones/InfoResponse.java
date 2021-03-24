package com.example.drones;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class InfoResponse {

    private HashMap<String, ArrayList<Location>> positions = new HashMap<>();
    private ArrayList<MarkedLocation> markedPositions = new ArrayList<>();

    private int waiting;
    private int connected;

    public InfoResponse(JSONObject obj) {
        JSONArray positionsJSON = null;
        try {
            positionsJSON = obj.getJSONArray("clients");


        for (int i = 0; i < positionsJSON.length(); i++) {
            String clientId = positionsJSON.getJSONObject(i).getString("userId");
            ArrayList<Location> positionsOfClient = new ArrayList<>();
            JSONArray positionsOfClientJSON = positionsJSON.getJSONObject(i).getJSONArray("locations");

            for (int j = 0; j < positionsOfClientJSON.length(); j++) {
                double longitude = positionsOfClientJSON.getJSONObject(j).getDouble("longitude");
                double latitude = positionsOfClientJSON.getJSONObject(j).getDouble("latitude");
                Location loc = new Location(clientId);
                loc.setLongitude(longitude);
                loc.setLatitude(latitude);
                positionsOfClient.add(loc);
            }
            positions.put(clientId, positionsOfClient);
        }


        positionsJSON = obj.getJSONArray("marks");

        for (int i = 0; i < positionsJSON.length(); i++) {
            for (int j = 0; j < positionsJSON.length(); j++) {
                double longitude = positionsJSON.getJSONObject(j).getDouble("longitude");
                double latitude = positionsJSON.getJSONObject(j).getDouble("latitude");
                int color = positionsJSON.getJSONObject(j).getInt("color");
                Location loc = new Location(this.toString());
                loc.setLongitude(longitude);
                loc.setLatitude(latitude);
                Location location = new Location(this.toString());
                location.setLongitude(longitude);
                location.setLatitude(latitude);
                markedPositions.add(new MarkedLocation(location, color));
            }
        }

        waiting = obj.getInt("waiting");
        connected = obj.getInt("connected");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, ArrayList<Location>> getPositions() {
        return positions;
    }

    public ArrayList<MarkedLocation> getMarkedPositions() {
        return markedPositions;
    }

    public int getWaiting() {
        return waiting;
    }

    public int getConnected() {
        return connected;
    }
}
