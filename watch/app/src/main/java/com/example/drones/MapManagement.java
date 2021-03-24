package com.example.drones;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.pluginscalebar.ScaleBarOptions;
import com.mapbox.pluginscalebar.ScaleBarPlugin;
import com.mapbox.pluginscalebar.ScaleBarWidget;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.drones.Constant.BASE_ZOOM;
import static com.example.drones.Constant.CONNECTED;
import static com.example.drones.Constant.MAP_ZOOM;
import static com.example.drones.Constant.MARKED_POINT_SIZE;
import static com.example.drones.Constant.USERID;

public class MapManagement implements OnMapReadyCallback {
    private View rootView;
    private View hud;
    private ViewGroup container;
    protected Activity activity;

    private MapboxMap mapboxMap;
    private MapView mapView;
    private HashMap<Integer, List<Double>> zoomLevels;

    private ArrayList<DiscoveredAreaCircle> circles = new ArrayList<>();
    private ArrayList<MarkedPositionCircle> markedPositionCircles = new ArrayList<>();
    private double zoom = BASE_ZOOM;
    private static LatLng COORDINATES = new LatLng(-34, 151);

    private ArrayList<Location> itinerary = new ArrayList<>();
    private HashMap<String, ArrayList<Location>> allPositions = new HashMap<>();
    private HashMap<String, ArrayList<Location>> allMarkedLocations = new HashMap<>();
    private ArrayList<Pair<MarkedPositionCircle, Location>> markedPositions = new ArrayList<>();
    public ServerSocket socket;
    private ImageView pos;

    private ScaleBarWidget scaleBarView;

    private GestureDetectorCompat mGestureDetector;
    volatile private int res;
    volatile private boolean markersToUpdate = false;
    private boolean newLocationBool = false;
    volatile private boolean askingToDisconnect = false;
    private boolean alertDisplayed = false;

    public MapManagement() {
        zoomLevels = new HashMap<>();
        // see here https://docs.mapbox.com/help/glossary/zoom-level/
        zoomLevels.put(12, new ArrayList<>(Arrays.asList(9.555, 8.978, 7.319, 4.777, 1.659)));
        zoomLevels.put(13, new ArrayList<>(Arrays.asList(4.777, 4.489, 3.660, 2.389, 0.830)));
        zoomLevels.put(14, new ArrayList<>(Arrays.asList(2.389, 2.245, 1.830, 1.194, 0.415)));
        zoomLevels.put(15, new ArrayList<>(Arrays.asList(1.194, 1.122, 0.915, 0.597, 0.207)));
        zoomLevels.put(16, new ArrayList<>(Arrays.asList(0.597, 0.561, 0.457, 0.299, 0.104)));
        zoomLevels.put(17, new ArrayList<>(Arrays.asList(0.299, 0.281, 0.229, 0.149, 0.052)));
        zoomLevels.put(18, new ArrayList<>(Arrays.asList(0.149, 0.140, 0.114, 0.075, 0.026)));
    }

    public void setSocket(ServerSocket socket) {
        this.socket = socket;
    }

    public ArrayList<Location>  getItinerary() {
        return itinerary;
    }

    public void computeZoomScale(Location p) {
        if(p.getLatitude() > 0) {
            if(p.getLatitude() < 10)
                zoom = zoomLevels.get(MAP_ZOOM).get(0);
            else if(p.getLatitude() < 30)
                zoom = zoomLevels.get(MAP_ZOOM).get(1);
            else if(p.getLatitude() < 50)
                zoom = zoomLevels.get(MAP_ZOOM).get(2);
            else if(p.getLatitude() < 70)
                zoom = zoomLevels.get(MAP_ZOOM).get(3);
            else
                zoom = zoomLevels.get(MAP_ZOOM).get(4);
        } else {
            if(p.getLatitude() > -10)
                zoom = zoomLevels.get(MAP_ZOOM).get(0);
            else if(p.getLatitude() > -30)
                zoom = zoomLevels.get(MAP_ZOOM).get(1);
            else if(p.getLatitude() > -50)
                zoom = zoomLevels.get(MAP_ZOOM).get(2);
            else if(p.getLatitude() > -70)
                zoom = zoomLevels.get(MAP_ZOOM).get(3);
            else
                zoom = zoomLevels.get(MAP_ZOOM).get(4);
        }
    }

    public void initMap(Activity activity, ViewGroup container, View rootView, View hud, ServerSocket socket) {
        this.activity = activity;
        this.container = container;
        this.rootView = rootView;
        this.hud = hud;
        this.socket = socket;

        bringViewToFront(container, hud);
        pos = hud.findViewById(R.id.pos);
        pos.setImageResource(R.drawable.pos);
        pos.setAdjustViewBounds(false);
        pos.getLayoutParams().height = (int)(4 / zoom);
        pos.getLayoutParams().width = (int)(4 / zoom);
        pos.requestLayout();

        mapView = rootView.findViewById(R.id.mapView);
        mapView.getMapAsync(this);
        mGestureDetector = new GestureDetectorCompat(activity, new MapGestureListener(this, activity, markedPositionCircles));
        initListeners();
    }

    private void initListeners() {

        rootView.findViewById(R.id.mapView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return false;
            }
        });

        hud.findViewById(R.id.zoomin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MAP_ZOOM < 18) {
                    MAP_ZOOM++;
                    computeZoomScale(itinerary.get(itinerary.size() - 1));
                    updateAreaTravelled();
                    updateMarkedPositions();

                    if((int)(4 / zoom) > 0) {
                        pos.getLayoutParams().height = (int) (4 / zoom);
                        pos.getLayoutParams().width = (int) (4 / zoom);
                    }
                    pos.requestLayout();
                }
                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .zoom(MAP_ZOOM)
                        .build());
            }
        });

        hud.findViewById(R.id.zoomout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MAP_ZOOM > 12) {
                    MAP_ZOOM --;
                    computeZoomScale(itinerary.get(itinerary.size() - 1));
                    updateAreaTravelled();
                    updateMarkedPositions();
                    if((int)(4 / zoom) > 0) {
                        pos.getLayoutParams().height = (int) (4 / zoom);
                        pos.getLayoutParams().width = (int) (4 / zoom);
                    }
                    pos.requestLayout();
                }

                mapboxMap.setCameraPosition(new CameraPosition.Builder()
                        .zoom(MAP_ZOOM)
                        .build());
            }
        });
    }
    public void downloadMapListener(Context context, double latNorth, double latSouth, double lngEast, double lngWest) {

        OfflineManager offlineManager = OfflineManager.getInstance(context);
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(latNorth, lngEast)) // Northeast
                .include(new LatLng(latSouth, lngWest)) // Southwest
                .build();
        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                mapboxMap.getStyle().getUri(),
                latLngBounds,
                12,
                18,
                context.getResources().getDisplayMetrics().density);
        // Create the region asynchronously
        offlineManager.createOfflineRegion(definition, null,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                        // Monitor the download progress
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {

                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {

                                // Calculate the download percentage
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                        0.0;

                                if (status.isComplete()) {
                                    // Download complete
                                    Log.d("SUCCESS", "Region downloaded successfully.");
                                    Toast.makeText(context, "SUCCESSFULLY DOWNLOADED MAP", Toast.LENGTH_LONG).show();

                                } else if (status.isRequiredResourceCountPrecise()) {
                                    Log.d("PERCENTAGE", String.valueOf(percentage));
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                // If an error occurs, print to logcat
                                Log.e("ERROR REASON", "onError reason: " + error.getReason());
                                Log.e("ERROR MSG", "onError message: " + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e("LIMIT", "Mapbox tile count limit exceeded: " + limit);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("ERROR", "Error: " + error);
                    }
                });
    }

    // Marker management

    protected void markPosition(float x, float y, int color) {
        MarkedPositionCircle markedPositionCircle = new MarkedPositionCircle(activity, x, y, color);
        markedPositionCircles.add(markedPositionCircle);
        container.addView(markedPositionCircle);
        float distance = (float) Math.sqrt(Math.pow(x - container.getHeight() / 2, 2) + Math.pow(y - container.getWidth() / 2, 2));
        float angle = (float) Math.atan2(x - container.getHeight() / 2, y - container.getWidth() / 2);
        distance *= zoom;
        Pair<Double, Double> pair = Util.computeGPSPosWithPointAndBearing(itinerary.get(itinerary.size() - 1), angle, distance / 1000);

        Location markedLocation = new Location(activity.toString());
        markedLocation.setLatitude(pair.first);
        markedLocation.setLongitude(pair.second);

        markedPositions.add(new Pair<>(markedPositionCircle, markedLocation));
        Thread newThread = new Thread(() -> {
            socket.send("{\"op\":\"mark\",\"userId\":\"" + USERID + "\",\"latitude\":\"" + markedLocation.getLatitude() + "\",\"longitude\":\"" + markedLocation.getLongitude() + "\",\"color\":\"" + color + "\"}");
        });
        newThread.start();
    }

    protected void deletePosition(float x, float y) {
        boolean toDelete = false;
        Pair<MarkedPositionCircle, Location> pairToDelete = null;
        for (Pair<MarkedPositionCircle, Location> pair : markedPositions) {
            if (Util.isPointInCircle(x, y, pair.first.getX(), pair.first.getY(), MARKED_POINT_SIZE)) {
                container.removeView(pair.first);
                pairToDelete = pair;
                toDelete = true;
                break;
            }
        }
        if (toDelete) {
            Location l = new Location(activity.toString());
            l.setLatitude(pairToDelete.second.getLatitude());
            l.setLongitude(pairToDelete.second.getLongitude());

            if (!Constant.IS_SERVER_CONNECTED) {
                Log.e("deletePosition", "can't send the order");
                return;
            }
            Thread newThread = new Thread(() -> {
                socket.send("{\"op\":\"markDelete\",\"userId\":\"" + USERID + "\",\"latitude\":\"" + l.getLatitude() + "\",\"longitude\":\"" + l.getLongitude() + "\"}");
            });
            newThread.start();
            markedPositions.remove(pairToDelete);
        }
    }


    protected void updateMarkedPositions(float deltaX, float deltaY) {
        newLocationBool = false;
        for (MarkedPositionCircle positionCircle : markedPositionCircles) {
            container.removeView(positionCircle);
        }

        ArrayList<Pair<MarkedPositionCircle, Location>> markedPositionsCp = new ArrayList<>();

        for (Pair<MarkedPositionCircle, Location> pair : markedPositions) {

            Location location = pair.second;
            float x = pair.first.getX() + deltaX;
            float y = pair.first.getY() + deltaY;
            MarkedPositionCircle circle = new MarkedPositionCircle(activity, x, y, pair.first.getColor());
            this.markedPositionCircles.add(circle);

            markedPositionsCp.add(new Pair<>(circle, location));
            container.removeView(pair.first);
            container.addView(circle);
        }
        markedPositions = markedPositionsCp;

        bringViewToFront(container, hud);

        if(scaleBarView != null) {
            bringViewToFront(container, scaleBarView);
        }
    }

    protected void updateMarkedPositions() {
        for (MarkedPositionCircle positionCircle : markedPositionCircles) {
            container.removeView(positionCircle);
        }
        ArrayList<Pair<MarkedPositionCircle, Location>> markedPositionsCp = new ArrayList<>();

        for (Pair<MarkedPositionCircle, Location> pair : markedPositions) {
            container.removeView(pair.first);


            Location previousLocation = itinerary.get(itinerary.size() - 1);
            Location location = pair.second;
            float x = container.getWidth() / 2;
            float y = container.getHeight() / 2;

            float Xangle = (float) (Math.cos(Math.toRadians(previousLocation.getLatitude())) * Math.sin(Math.toRadians(previousLocation.getLongitude()) - Math.toRadians(location.getLongitude())));
            float Yangle = (float) (Math.cos(Math.toRadians(location.getLatitude())) * Math.sin(Math.toRadians(previousLocation.getLatitude())) -
                    Math.sin(Math.toRadians(location.getLatitude())) * Math.cos(Math.toRadians(previousLocation.getLatitude())) *
                            Math.cos(Math.toRadians(previousLocation.getLongitude()) - Math.toRadians(location.getLongitude())));
            float angle = (float) (360 + Math.toDegrees(Math.atan2(Xangle, Yangle))) % 360;

            x = (float) (x - (( previousLocation.distanceTo(location) / this.zoom ) * Math.sin(Math.toRadians(angle))));
            y = (float) (y - (( previousLocation.distanceTo(location) / this.zoom ) * Math.cos(Math.toRadians(angle))));

            MarkedPositionCircle circle = new MarkedPositionCircle(activity, x, y, pair.first.getColor());
            this.markedPositionCircles.add(circle);

            markedPositionsCp.add(new Pair<>(circle, location));
            container.removeView(pair.first);
            container.addView(circle);
        }
        markedPositions = markedPositionsCp;
        bringViewToFront(container, hud);

        if(scaleBarView != null) {
            bringViewToFront(container, scaleBarView);
        }
    }

    // Drone control


    protected void flyTo(float x, float y) {
        float distance = (float) Math.sqrt(Math.pow(x - container.getWidth() / 2, 2) + Math.pow(y - container.getWidth() / 2, 2));
        float angle = (float) Math.atan2(y - container.getHeight() / 2, x - container.getWidth() / 2);
        distance *= zoom;
        Pair<Double, Double> pair = Util.computeGPSPosWithPointAndBearing(itinerary.get(itinerary.size() - 1), angle, distance / 1000);

        Location targetLocation = new Location(activity.toString());
        targetLocation.setLatitude(pair.first);
        targetLocation.setLongitude(pair.second);

        Thread newThread = new Thread(() -> {
            res = socket.flyTo(USERID, targetLocation);
        });
        newThread.start();
        try {
            newThread.join();
            if (res == 1) {
                Toast.makeText(activity.getApplicationContext(), "Drone envoyé", Toast.LENGTH_LONG).show();
            } else if (res == 0) {
                Toast.makeText(activity.getApplicationContext(), "Vous n'êtes pas connecté au drone", Toast.LENGTH_LONG).show();
            } else if (res == -1) {
                Toast.makeText(activity.getApplicationContext(), "Veuillez faire décoller le drone avant", Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void picture(float x, float y, int altitude) {
        float distance = (float) Math.sqrt(Math.pow(x - container.getWidth() / 2, 2) + Math.pow(y - container.getWidth() / 2, 2));
        float angle = (float) Math.atan2(y - container.getHeight() / 2, x - container.getWidth() / 2);
        distance *= zoom;
        Pair<Double, Double> pair = Util.computeGPSPosWithPointAndBearing(itinerary.get(itinerary.size() - 1), angle, distance / 1000);

        Location targetLocation = new Location(activity.toString());
        targetLocation.setLatitude(pair.first);
        targetLocation.setLongitude(pair.second);

        Thread newThread = new Thread(() -> {
            res = socket.picture(USERID, targetLocation, altitude);
        });
        newThread.start();
        try {
            newThread.join();
            if (res == 1) {
                Toast.makeText(activity.getApplicationContext(), "Drone envoyé", Toast.LENGTH_LONG).show();
            } else if (res == 0) {
                Toast.makeText(activity.getApplicationContext(), "Vous n'êtes pas connecté au drone", Toast.LENGTH_LONG).show();
            } else if (res == -1) {
                Toast.makeText(activity.getApplicationContext(), "Veuillez faire décoller le drone avant", Toast.LENGTH_LONG).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Area travelled management

    public void updateAreaTravelled() {
        for (DiscoveredAreaCircle circle : circles) {
            container.removeView(circle);
        }

        float x = container.getWidth() / 2;
        float y = container.getHeight() / 2;
        float pixelPerMeter = (float) (1 / zoom);
        Location previousLocation = null;
        Location currentLocation = itinerary.get(itinerary.size() - 1);

        float firstX = 0;
        float firstY = 0;
        // Pour chaque location en partant de la derniere AKA la position courante
        for (int i = itinerary.size() - 1; i >= 0; i--) {
            Location location = itinerary.get(i);
            //computeZoomScale(location);

            if (previousLocation != null) {
                // Calcule l'angle entre la position et la position précédente

                float Xangle = (float) (Math.cos(Math.toRadians(previousLocation.getLatitude())) * Math.sin(Math.toRadians(previousLocation.getLongitude()) - Math.toRadians(location.getLongitude())));
                float Yangle = (float) (Math.cos(Math.toRadians(location.getLatitude())) * Math.sin(Math.toRadians(previousLocation.getLatitude())) -
                        Math.sin(Math.toRadians(location.getLatitude())) * Math.cos(Math.toRadians(previousLocation.getLatitude())) *
                                Math.cos(Math.toRadians(previousLocation.getLongitude()) - Math.toRadians(location.getLongitude())));
                float angle = (float) (360 + Math.toDegrees(Math.atan2(Xangle, Yangle))) % 360;

                // Set l'angle du triangle de localisation
                if (previousLocation == currentLocation) {
                    pos.setRotation(angle);
                }

                // set la position des cercles en fonction du précédent
                x = (float) (x - previousLocation.distanceTo(location) * pixelPerMeter * Math.sin(Math.toRadians(angle)));
                y = (float) (y + previousLocation.distanceTo(location) * pixelPerMeter * Math.cos(Math.toRadians(angle)));
                if (i == itinerary.size() - 2) {
                    firstX = x;
                    firstY = y;
                }
            }

            DiscoveredAreaCircle circle = new DiscoveredAreaCircle(activity, x, y, zoom);
            this.circles.add(circle);
            container.addView(circle);
            previousLocation = location;
        }

        float deltaX = (firstX - container.getWidth()/2);
        float deltaY = (firstY - container.getHeight()/2);
        updateMarkedPositions(deltaX, deltaY);
    }

    // Map management

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendLocationAndUpdateMap(Location p) {

        Thread newThread = new Thread(() -> {
            USERID = socket.getId(USERID);
            //Récupère le nombre de secondes depuis epoch pour le versionning
            long time = Instant.now().getEpochSecond();
            socket.send("{\"op\":\"location\",\"userId\":\"" + USERID + "\",\"latitude\":\"" + p.getLatitude() + "\",\"longitude\":\"" + p.getLongitude() + "\",\"time\":\"" + time + "\"}");
        });

        newThread.start();

        if(container != null && itinerary.size() > 0) {
            newLocationBool = true;
            updateAreaTravelled();
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        // Permits to disable all the mapbox callbacks on events (ex : double tap to zoom)
        mapboxMap.getUiSettings().setAllGesturesEnabled(false);
        ScaleBarPlugin scaleBarPlugin = new ScaleBarPlugin(mapView, mapboxMap);

        // Create a ScaleBarOptions object to use the Plugin's default styling
        /* scaleBarView = scaleBarPlugin.create(new ScaleBarOptions(activity));*/
        scaleBarPlugin.setEnabled(false);
        mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                ScaleBarPlugin scaleBarPlugin = new ScaleBarPlugin(mapView, mapboxMap);

                // Set the custom styling via a ScaleBarOptions object
                ScaleBarOptions scaleBarOptions = new ScaleBarOptions(activity)
                        .setTextColor(R.color.mapbox_blue)
                        .setTextSize(15f)
                        .setBarHeight(10f)
                        .setBorderWidth(2f)
                        .setMetricUnit(true)
                        .setRefreshInterval(15)
                        .setMarginTop(320f)
                        .setMarginLeft(100f)
                        .setTextBarMargin(10f);

                // Create a ScaleBarOptions object to use the Plugin's default styling
                scaleBarView = scaleBarPlugin.create(scaleBarOptions);
                mapView.removeView(scaleBarView);
                container.addView(scaleBarView);

            }
        });
        if (itinerary.size() > 0) {
            Location currentLocation = itinerary.get(itinerary.size() - 1);
            updateCameraMapPosition(currentLocation);
        }
    }


    public void updateCameraMapPosition(Location p) {
        COORDINATES.setLongitude(p.getLongitude());
        COORDINATES.setLatitude(p.getLatitude());
        CameraPosition position = new CameraPosition.Builder()
                .target(COORDINATES)
                .zoom(MAP_ZOOM)
                .tilt(0)
                .build();
        if (mapboxMap != null) mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 10);
    }

    public void getAllLocations() {
        markersToUpdate = false;
        Thread updateLocation = new Thread(() -> {
            USERID = socket.getId(USERID);
            InfoResponse response = socket.getInfos(USERID);
            ArrayList<Location> newLocation = new ArrayList<>();
            if (response.getPositions().values().size() > 0) newLocationBool = true;
            for (ArrayList<Location> locations : response.getPositions().values()) {
                newLocation.addAll(locations);
            }

            ArrayList<Location> locationsToDelete = new ArrayList<>();
            for (Location location1 : newLocation) {
                for (Location location2 : newLocation) {
                    if (location1.getLatitude() == location2.getLatitude() && location1.getLongitude() == location2.getLongitude() && !location1.equals(location2))
                        locationsToDelete.add(location2);
                }
                for (Location location2 : itinerary) {
                    if (location1.getLatitude() == location2.getLatitude() && location1.getLongitude() == location2.getLongitude())
                        locationsToDelete.add(location1);
                }
            }

            newLocation.removeAll(locationsToDelete);
            itinerary.addAll(newLocation);
            if (newLocation.size() > 0) markersToUpdate = true;

            for (MarkedLocation markedLocation : response.getMarkedPositions()) {
                Location location = markedLocation.getLocation();
                boolean next = false;
                for (Pair<MarkedPositionCircle, Location> existingLocation : markedPositions) {
                    if (existingLocation.second.getLatitude() == location.getLatitude() && existingLocation.second.getLongitude() == location.getLongitude())
                        next = true;
                }
                if (!next) {
                    Location previousLocation = itinerary.get(itinerary.size() - 1);
                    float x = container.getWidth() / 2f;
                    float y = container.getHeight() / 2f;

                    float Xangle = (float) (Math.cos(Math.toRadians(previousLocation.getLatitude())) * Math.sin(Math.toRadians(previousLocation.getLongitude()) - Math.toRadians(location.getLongitude())));
                    float Yangle = (float) (Math.cos(Math.toRadians(location.getLatitude())) * Math.sin(Math.toRadians(previousLocation.getLatitude())) -
                            Math.sin(Math.toRadians(location.getLatitude())) * Math.cos(Math.toRadians(previousLocation.getLatitude())) *
                                    Math.cos(Math.toRadians(previousLocation.getLongitude()) - Math.toRadians(location.getLongitude())));
                    float angle = (float) (360 + Math.toDegrees(Math.atan2(Xangle, Yangle))) % 360;

                    x = (float) (x - ((previousLocation.distanceTo(location) / this.zoom) * Math.cos(Math.toRadians(angle))));
                    y = (float) (y - ((previousLocation.distanceTo(location) / this.zoom) * Math.sin(Math.toRadians(angle))));

                    MarkedPositionCircle circle = new MarkedPositionCircle(activity, x, y, markedLocation.getColor());
                    this.markedPositionCircles.add(circle);

                    markedPositions.add(new Pair<>(circle, location));
                }
            }
            if (response.getWaiting() >= 1 && Constant.CONNECTED) {
                askingToDisconnect = true;
            }

        });

        updateLocation.start();
        try {
            updateLocation.join();
            if (markersToUpdate && MenuFragment.position == 3) updateMarkedPositions();
            if (askingToDisconnect && !alertDisplayed) {
                alertDisplayed = true;
                new AlertDialog.Builder(activity)
                        .setMessage("Another user wants to control the drone. Do you want to disconnect ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                askingToDisconnect = false;
                                Thread newThread = new Thread(() -> {
                                    USERID = socket.getId(USERID);
                                    res = socket.giveBackControl(USERID);
                                });
                                newThread.start();
                                try {
                                    newThread.join();
                                    if (res == 1) {
                                        Toast.makeText(activity.getApplicationContext(), "Déconnecté du drone", Toast.LENGTH_LONG).show();
                                        CONNECTED = false;
                                    } else {
                                        Toast.makeText(activity.getApplicationContext(), "Erreur", Toast.LENGTH_LONG).show();
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                alertDisplayed = false;
                            }
                        })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    alertDisplayed = false;
                                    Thread refuseDisconnectionRequest = new Thread(() -> {
                                        socket.sendRefuseDisconnetionRequest(USERID);
                                    });
                                    refuseDisconnectionRequest.start();
                                    alertDisplayed = false;
                                }
                            }).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // Utils

    public void bringViewToFront(ViewGroup parent, View child){
        parent.removeView(child);
        parent.addView(child);
        child.bringToFront();
    }

    public void cleanCircles() {
        for (DiscoveredAreaCircle circle : circles) {
            container.removeView(circle);
        }

        for (Pair<MarkedPositionCircle, Location> pair : markedPositions) {
            container.removeView(pair.first);
        }
    }

    public void clearView() {
        if(hud != null)
            container.removeView(hud);
        if(scaleBarView != null && scaleBarView.getParent() == container)
            container.removeView(scaleBarView);
        if(scaleBarView != null)
            scaleBarView = null;
        cleanCircles();
    }
}