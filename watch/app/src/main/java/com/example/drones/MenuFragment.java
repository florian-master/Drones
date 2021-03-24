package com.example.drones;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.mapbox.mapboxsdk.Mapbox;


import java.util.ArrayList;
import java.util.Map;

import static com.example.drones.Constant.*;

public class MenuFragment extends Fragment {
    private View rootView;
    private View hud;
    private ViewGroup container;
    private Activity activity;
    private LocationCallback locationCallback;
    private MapManagement myMap;

    volatile private int res;
    volatile public static int position = 0;

    private LocationRequest request;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public ServerSocket socket;

    public MenuFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.wifimanager, container, false);
        hud = inflater.inflate(R.layout.hud, container, false);
        this.activity = getActivity();
        this.container = container;
        this.socket = new ServerSocket();
        myMap = new MapManagement();

//        myMap.cleanCircles();
        WifiManagement wifiManagement = new WifiManagement(getContext(), rootView, activity);

        // must be connected to a network
        Log.i("wifi IP address", wifiManagement.getIpAddress());
        // must wait for scan to be over
//            Log.i("wifi hotspot", wifiManagement.getAvailableNetworks().toString());

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(activity, getString(R.string.mapbox_access_token));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);

        request = new LocationRequest()
                .setFastestInterval(DELAY)
                .setInterval(DELAY)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(activity)
                .checkLocationSettings(builder.build());

        result.addOnFailureListener(activity, e -> {
            if (e instanceof ResolvableApiException) {
                ResolvableApiException resolvable = (ResolvableApiException) e;
                try {
                    resolvable.startResolutionForResult(activity, REQUEST_CODE);
                } catch (IntentSender.SendIntentException sendIntentException) {
                    sendIntentException.printStackTrace();
                }
            }
        });
        myMap.setSocket(socket);
        locationCallback = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                // On récupère la position
                Location p = locationResult.getLocations().get(0);
                ArrayList<Location> itinerary = myMap.getItinerary();
                if (itinerary.size() > 0) {
                    if (itinerary.get(itinerary.size() - 1).distanceTo(p) > 2) {
                        System.out.println("Distance supérieure à 2 mètres entre les deux dernière positions");
                        itinerary.add(p);
                        myMap.sendLocationAndUpdateMap(p);
                        myMap.updateCameraMapPosition(p);
                    }
                } else {
                    itinerary.add(p);
                    System.out.println("Pas de location dans l'itinéraire");
                    myMap.sendLocationAndUpdateMap(p);
                }
                if (position == 3)
                    myMap.getAllLocations();
            }
        };
        return rootView;
    }

    public void changeView(LayoutInflater inflater, int pos, String[] menus) {
        int resourceName = getResources().getIdentifier(menus[pos], "layout", "com.example.drones");
        container.removeView(rootView);
        myMap.clearView();
        rootView = inflater.inflate(resourceName, container, false);
        container.addView(rootView);

        this.position = pos;
        if(pos == 1) {
            final Button coarse = rootView.findViewById(R.id.coarse_location);
            coarse.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);
                    Toast.makeText(getActivity().getApplicationContext(), "Permission COARSE accordée", Toast.LENGTH_LONG).show();
                }
            });

            final Button fine = rootView.findViewById(R.id.fine_location);
            fine.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(
                            activity,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                    Toast.makeText(getActivity().getApplicationContext(), "Permission FINE accordée", Toast.LENGTH_LONG).show();
                }
            });
        } else if (pos == 2) {
            if (CONNECTED) {
                rootView.findViewById(R.id.takeoff).setEnabled(true);
                rootView.findViewById(R.id.landing).setEnabled(true);
                rootView.findViewById(R.id.disconnect).setEnabled(true);
                rootView.findViewById(R.id.share).setEnabled(true);
                rootView.findViewById(R.id.connect).setEnabled(false);
            } else {
                rootView.findViewById(R.id.connect).setEnabled(true);
                rootView.findViewById(R.id.disconnect).setEnabled(false);
                rootView.findViewById(R.id.share).setEnabled(false);
                rootView.findViewById(R.id.takeoff).setEnabled(false);
                rootView.findViewById(R.id.landing).setEnabled(false);
            }

            rootView.findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Thread newThread = new Thread(() -> {
                        USERID = socket.getId(USERID);
                        res = socket.requestControl(USERID);
                    });
                    newThread.start();
                    try {
                        newThread.join();
                        if (res == 1) {
                            rootView.findViewById(R.id.takeoff).setEnabled(true);
                            rootView.findViewById(R.id.landing).setEnabled(true);
                            rootView.findViewById(R.id.disconnect).setEnabled(true);
                            rootView.findViewById(R.id.share).setEnabled(true);
                            rootView.findViewById(R.id.connect).setEnabled(false);
                            CONNECTED = true;
                            Toast.makeText(getActivity().getApplicationContext(), "Connecté au drone", Toast.LENGTH_LONG).show();
                        } else if (res == 0) {
                            Toast.makeText(getActivity().getApplicationContext(), "Un utilisateur est déjà connecté au drone", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Erreur", Toast.LENGTH_LONG).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            rootView.findViewById(R.id.disconnect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Are you sure you want to disconnect from the drone?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Thread newThread = new Thread(() -> {
                                        USERID = socket.getId(USERID);
                                        res = socket.giveBackControl(USERID);
                                    });
                                    newThread.start();
                                    try {
                                        newThread.join();
                                        if (res == 1) {
                                            rootView.findViewById(R.id.connect).setEnabled(true);
                                            rootView.findViewById(R.id.disconnect).setEnabled(false);
                                            rootView.findViewById(R.id.share).setEnabled(false);
                                            rootView.findViewById(R.id.takeoff).setEnabled(false);
                                            rootView.findViewById(R.id.landing).setEnabled(false);
                                            CONNECTED = false;
                                            Toast.makeText(getActivity().getApplicationContext(), "Déconnecté du drone", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "Erreur", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                }
            });

            rootView.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Are you sure you want to the drone to share data to all the users")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Thread newThread = new Thread(() -> {
                                        USERID = socket.getId(USERID);
                                        res = socket.share(USERID);
                                    });
                                    newThread.start();
                                    try {
                                        newThread.join();
                                        if (res == 1) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Partage des données débuté", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "Erreur", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();

                }
            });

            rootView.findViewById(R.id.takeoff).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Thread newThread = new Thread(() -> {
                        USERID = socket.getId(USERID);
                        res = socket.takeOff(USERID);
                    });
                    newThread.start();
                    try {
                        newThread.join();
                        if (res == 1) {
                            rootView.findViewById(R.id.takeoff).setEnabled(false);
                            rootView.findViewById(R.id.landing).setEnabled(true);
                            Toast.makeText(getActivity().getApplicationContext(), "Décollage", Toast.LENGTH_LONG).show();
                        } else if (res == 0) {
                            Toast.makeText(getActivity().getApplicationContext(), "Le drone a déjà décollé", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), "Erreur", Toast.LENGTH_LONG).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            rootView.findViewById(R.id.landing).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Thread newThread = new Thread(() -> {
                        USERID = socket.getId(USERID);
                        res = socket.landing(USERID);
                    });
                    newThread.start();
                    try {
                        newThread.join();
                        if (res == 1) {
                            rootView.findViewById(R.id.landing).setEnabled(false);
                            rootView.findViewById(R.id.takeoff).setEnabled(true);
                            Toast.makeText(getActivity().getApplicationContext(), "Atterrissage", Toast.LENGTH_LONG).show();
                        } else if (res == 0) {
                            Toast.makeText(getActivity().getApplicationContext(), "Le drone est déjà au sol", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Erreur", Toast.LENGTH_LONG).show();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (pos == 3) {
            myMap.initMap(activity, container, rootView, hud, socket);
            myMap.updateAreaTravelled();
            myMap.updateMarkedPositions();

        } else if (pos == 4) {
            myMap.cleanCircles();
            LoadMap.init(rootView);

            final Button load = rootView.findViewById(R.id.load);

            load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (LoadMap.checkInputs()) {
                        if (!Constant.IS_SERVER_CONNECTED) {
                            Toast.makeText(activity, "Erreur : vous n'êtes pas connecté à internet", Toast.LENGTH_LONG).show();
                            return;
                        }
                        Map<String, Double> res = LoadMap.getInputs();
                        if (res != null)
                            myMap.downloadMapListener(activity, res.get("latNorth"), res.get("latSouth"), res.get("lngEast"), res.get("lngWest"));
                    } else Toast.makeText(activity, "saisie invalide", Toast.LENGTH_LONG).show();

                }
            });
        } else {
            myMap.cleanCircles();
        }
    }

    public void startLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.e("permission denied", "use location");
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onStart() {

        super.onStart();
        startLocationUpdates();
    }

    @Override
    public void onResume() {

        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
