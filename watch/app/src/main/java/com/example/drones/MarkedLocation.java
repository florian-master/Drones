package com.example.drones;

import android.location.Location;

public class MarkedLocation {
    private Location location;
    private int color;

    public MarkedLocation(Location location, int color) {
        this.location = location;
        this.color = color;
    }

    public Location getLocation() {
        return location;
    }

    public int getColor() {
        return color;
    }
}
