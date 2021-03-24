package com.example.drones;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.util.HashMap;
import java.util.Map;
import static com.example.drones.Constant.MAX_LATITUDE;
import static com.example.drones.Constant.MAX_LONGITUDE;

final public class LoadMap {

    // Load map inputs
    private static EditText latNorth;
    private static EditText latSouth;
    private static EditText lngEast;
    private static EditText lngWest;
    private static TextView error_msg;
    private static Button load;

    public static void init(View rootView) {

        // Get the load map inputs
        latNorth = rootView.findViewById(R.id.latNth);
        latSouth = rootView.findViewById(R.id.latSth);
        lngEast = rootView.findViewById(R.id.lngEst);
        lngWest = rootView.findViewById(R.id.lngWst);
        load = rootView.findViewById(R.id.load);
        error_msg = rootView.findViewById(R.id.error_message);
        error_msg.setVisibility(View.INVISIBLE);

    }

    public static boolean checkInputs() {
        error_msg.setVisibility(View.INVISIBLE);

        if (!Util.inBound(latNorth.getText().toString(), MAX_LATITUDE)) {
            error_msg.setText("Erreur lat. Nord");
            error_msg.setVisibility(View.VISIBLE);
            return false;
        }

        if (!Util.inBound(latSouth.getText().toString(), MAX_LATITUDE)) {
            error_msg.setText("Erreur lat. Sud");
            error_msg.setVisibility(View.VISIBLE);
            return false;
        }

        if (!Util.inBound(lngEast.getText().toString(), MAX_LONGITUDE)) {
            error_msg.setText("Erreur lng. Est");
            error_msg.setVisibility(View.VISIBLE);
            return false;
        }

        if (!Util.inBound(lngWest.getText().toString(), MAX_LONGITUDE)) {
            error_msg.setText("Erreur lng. Est");
            error_msg.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    public static Map<String, Double> getInputs() {
        if(checkInputs()) {
            Map<String, Double> inputs = new HashMap<>();
            inputs.put("latNorth", Double.valueOf(latNorth.getText().toString()));
            inputs.put("latSouth", Double.valueOf(latSouth.getText().toString()));
            inputs.put("lngEast", Double.valueOf(lngEast.getText().toString()));
            inputs.put("lngWest", Double.valueOf(lngWest.getText().toString()));
            return inputs;
        }
        return null;
    }

    public static void printInputs() {
        Log.i("load map", latNorth.getText().toString());
        Log.i("load map", latSouth.getText().toString());
        Log.i("load map", lngEast.getText().toString());
        Log.i("load map", lngWest.getText().toString());
    }
}
