package com.example.drones;

import android.location.Location;
import android.util.Pair;

import java.util.ArrayList;

public final class Util {

    static public Pair<Double, Double> computeGPSPosWithPointAndBearing(Location location, double bearing, double distance) {
        double R = Constant.EARTH_RADIUS;

        double lat1 = Math.toRadians(location.getLatitude());
        double lon1 = Math.toRadians(location.getLongitude());

        double lat = Math.asin( Math.sin(lat1) * Math.cos(distance/R) + Math.cos(lat1) * Math.sin(distance/R) * Math.cos(bearing));
        double lon = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(distance/R) * Math.cos(lat1), Math.cos(distance/R) - Math.sin(lat1) * Math.sin(lat));

        lat = Math.toDegrees(lat);
        lon = Math.toDegrees(lon);

        return new Pair<Double, Double>(lat, lon);
    }

    static public boolean isPointInCircle(float x, float y, float xCenter, float yCenter, float radius) {
        return Math.pow((x - xCenter),2) + Math.pow((y - yCenter),2) < Math.pow(radius,2);
    }

    /**
     * Check if the value belongs to the range [-limit, limit]
     * @param value the value to test
     * @param limit the extreme range
     * @return true if the value belongs to the range [-limit, limit] false otherwise
     */
    static public boolean inBound(String value, int limit) {
        if (value == null || value.equals("")) {
            return false;
        }

        float val = Float.parseFloat(value);
        return -limit <= val && val <= limit;
    }

    static public void connected() {
        Constant.IS_SERVER_CONNECTED = true;
    }

    static public void disconnected() {
        Constant.IS_SERVER_CONNECTED = false;
    }
}
