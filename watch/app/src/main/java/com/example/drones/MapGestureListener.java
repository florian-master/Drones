package com.example.drones;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.drones.Constant.IS_SERVER_CONNECTED;

public class MapGestureListener extends GestureDetector.SimpleOnGestureListener {

    private MapManagement mapManagement;
    private Activity activity;
    ArrayList<MarkedPositionCircle> markedPositionCircles;

    public MapGestureListener(MapManagement mapManagement, Activity activity, ArrayList<MarkedPositionCircle> markedPositionCircles) {
        this.mapManagement = mapManagement;
        this.activity = activity;
        this.markedPositionCircles = markedPositionCircles;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return super.onSingleTapUp(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        AlertDialog.Builder moveDroneOrSetPoint = new AlertDialog.Builder(mapManagement.activity);
        moveDroneOrSetPoint.setTitle("Action");
        moveDroneOrSetPoint.setPositiveButton("Envoyer drone à cette position", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                    mapManagement.flyTo(e.getRawX(), e.getRawY());
            }
        });

        moveDroneOrSetPoint.setNegativeButton("Envoyer le drone prendre des photos à cette position et revenir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                    CharSequence[] valuesTxt = new CharSequence[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
                    int[] values = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(mapManagement.activity);
                    mBuilder.setTitle("Altitude des photos");
                    mBuilder.setItems(valuesTxt, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mapManagement.picture(e.getRawX(), e.getRawY(), values[i]);
                        }
                    });
                    mBuilder.create().show();
            }
        });

        MarkedPositionCircle circleToDelete = null;
        for (MarkedPositionCircle mark : MapGestureListener.this.markedPositionCircles) {
            if (Util.isPointInCircle(e.getRawX(), e.getRawY(), mark.getX(), mark.getY(), Constant.MARKED_POINT_SIZE)) {
                circleToDelete = mark;
            }
        }
        if (Objects.isNull(circleToDelete)) {
            moveDroneOrSetPoint.setNeutralButton("Placer un point de repère", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    CharSequence[] colors = new CharSequence[]{"Rouge", "Vert", "Bleu", "Jaune"};
                    int[] colorCodes = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(mapManagement.activity);
                    mBuilder.setTitle("Nouveau point");
                    mBuilder.setItems(colors, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mapManagement.markPosition(e.getRawX(), e.getRawY(), colorCodes[i]);
                            Toast.makeText(activity.getApplicationContext(), "Marquage " + colors[i] + " ajouté", Toast.LENGTH_LONG).show();
                        }
                    });

                    mBuilder.create().show();
                }
            });
        }
        else {
            moveDroneOrSetPoint.setNeutralButton("Supprimer le point de repère", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mapManagement.deletePosition(e.getRawX(), e.getRawY());
                }
            });
        }
            moveDroneOrSetPoint.create().show();
            super.onLongPress(e);
    }
}
