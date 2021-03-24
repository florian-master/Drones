package com.example.drones;

import android.location.Location;
import android.util.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerSocket {

    public ServerSocket() {
    }

    public String getId(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"connexion\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            String userIdServer = obj.getString("userId");
            System.out.println("userId send by server : " + userIdServer);
            pw.close();
            s.close();
            return userIdServer;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public HashMap<String, ArrayList<Location>> getPositionsOfAll(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"getLocations\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            System.out.println("Objet json reçu : " + obj);

            JSONArray positionsJSON = obj.getJSONArray("clients");
            HashMap<String, ArrayList<Location>> positions = new HashMap<>();

            // Pour chaque client envoyé
            for (int i = 0; i < positionsJSON.length(); i++) {
                String clientId = positionsJSON.getJSONObject(i).getString("userId");
                ArrayList<Location> positionsOfClient = new ArrayList<>();
                JSONArray positionsOfClientJSON = positionsJSON.getJSONObject(i).getJSONArray("locations");

                // Pour chaque localisation de chaque client envoyé
                for (int j = 0; j < positionsOfClientJSON.length(); j++) {
                    double longitude = positionsOfClientJSON.getJSONObject(j).getDouble("longitude");
                    double latitude = positionsOfClientJSON.getJSONObject(j).getDouble("latitude");
                    Location loc = new Location(clientId);
                    loc.setLongitude(longitude);
                    loc.setLatitude(latitude);
                    positionsOfClient.add(loc);
                }
                // On ajoute a la HashMap le couple (userId,[[0.151,-5.478],[7.256,-3.231],...])
                positions.put(clientId, positionsOfClient);
            }
            pw.close();
            s.close();
            return positions;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public ArrayList<MarkedLocation> getMarkedLocations(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"getMarkedLocations\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            System.out.println("Objet json reçu : " + obj);

            JSONArray positionsJSON = obj.getJSONArray("marks");
            ArrayList<MarkedLocation> positions = new ArrayList<>();

            // Pour chaque client envoyé
            for (int i = 0; i < positionsJSON.length(); i++) {
                // Pour chaque localisation de chaque client envoyé
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
                    positions.add(new MarkedLocation(location, color));
                }
            }
            pw.close();
            s.close();
            return positions;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public int send(String message) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Lecture des données envoyées par le serveur
            //in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            //System.out.println(in.readLine());
            // Envoi du message au serveur
            pw.write(message);
            pw.flush();
            pw.flush();
            // Lecture du message envoyé par le serveur en réponse a l'envoi de notre message
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            System.out.println(in.readLine());
            pw.close();
            s.close();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void sendUserPositions(ArrayList<Location> itinerary, String userId) {
        if (itinerary.size() > 0) {
            StringBuilder locationHistory = new StringBuilder();
            locationHistory.append("{\"op\":\"locations\" ,\"userId\":\"" + userId + "\",\"locations\":[");
            for (Location location : itinerary) {
                locationHistory.append("{\"longitude\":" + location.getLongitude() + ",\"latitude\":" + location.getLatitude() + "},");
            }
            locationHistory.deleteCharAt(locationHistory.length() - 1);
            locationHistory.append("]}");
            this.send(locationHistory.toString());
        }
    }

    public void sendMarkedPosition(ArrayList<Pair<Location, String>> pairs, String userId) {
        if (pairs.size() > 0) {
            StringBuilder markedPositionHistory = new StringBuilder();
            markedPositionHistory.append("{\"op\":\"marks\" ,\"userId\":\"" + userId + "\",\"locations\":[");
            for (Pair<Location, String> pair : pairs) {
                markedPositionHistory.append("{\"longitude\":" + pair.first.getLongitude() + ",\"latitude\":" + pair.first.getLatitude() + ",\"color\":\"" + pair.second + "\"" + "},");
            }
            markedPositionHistory.deleteCharAt(markedPositionHistory.length() - 1);
            markedPositionHistory.append("]}");
            this.send(markedPositionHistory.toString());
        }
    }

    public int requestControl(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"requestControl\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande de controle du drone : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int giveBackControl(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"giveBackControl\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande de rendre la main du controle du drone : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int takeOff(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"takeOff\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande de décollage : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int landing(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"landing\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande d'atterrissage : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int flyTo(String userId, Location location) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"flyTo\",\"userId\":\"" + userId + "\",\"longitude\":\"" + location.getLongitude() + "\",\"latitude\":\"" + location.getLatitude() + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande de flyTo : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int picture(String userId, Location location, int altitude) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"share\",\"userId\":\"" + userId + "\",\"longitude\":\"" + location.getLongitude() + "\",\"latitude\":\"" + location.getLatitude() + "\",\"altitude\":\"" + altitude + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande de picture : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int share(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"share\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("Demande de picture : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public InfoResponse getInfos(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"getInfos\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            pw.close();
            s.close();
            return new InfoResponse(obj);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int sendRefuseDisconnetionRequest(String userId) {
        try {
            Socket s = new Socket(Constant.IP_ADDRESS, Constant.PORT);
            PrintWriter pw = new PrintWriter(s.getOutputStream());
            // Envoi du message au serveur
            pw.write("{\"op\":\"refuseDisconnectionRequest\",\"userId\":\"" + userId + "\"}");
            pw.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result = in.readLine();
            JSONObject obj = new JSONObject(result);
            int operationResult = obj.getInt("response");
            System.out.println("refuseDisconnectionRequest : " + operationResult);
            pw.close();
            s.close();
            return operationResult;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
