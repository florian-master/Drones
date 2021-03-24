package com.example.drones;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

final public class WifiManagement implements AdapterView.OnItemClickListener {
    private final Activity activity;
    private final Context context;
    private final TextView text_msg;
    private final Button scan_btn;

    private WifiManager wifiManager;
    private ArrayList<String> wifiResults = new ArrayList<>();
    private List<ScanResult> wifiScanResults;
    private ArrayAdapter<String> arrayAdapter;

    public WifiManagement(Context context, View rootView, Activity activity) {
        this.activity = activity;
        this.context = context;

        scan_btn = rootView.findViewById(R.id.scan);
        ListView wifiList = rootView.findViewById(R.id.wifi_list);
        text_msg = rootView.findViewById(R.id.text_msg);

        wifiManager = (android.net.wifi.WifiManager) activity.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            text_msg.setText("Activé le WiFi svp!");
            scan_btn.setText("Act. WiFi");
        } else {
            scan_btn.setText("Scan");
        }

        arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, wifiResults);
        wifiList.setAdapter(arrayAdapter);
        wifiList.setOnItemClickListener(this);
        scanWifi();

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                    scan_btn.setText("Scan");
                    text_msg.setText("Aucun point access trouvé");
                    Log.i ("wifi", "wifi enabled");
                } else {
                    scanWifi();
                }
            }
        });
    }

    private void scanWifi() {
        text_msg.setVisibility(View.VISIBLE);
        if (!wifiManager.isWifiEnabled()) {
            text_msg.setText("Activé le WiFi svp!");
            scan_btn.setText("Act. WiFi");
            return;
        }
        text_msg.setText("Recherche de point d'access ...");

        wifiResults.clear();
        arrayAdapter.clear();

        BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiScanResults = wifiManager.getScanResults();
                activity.unregisterReceiver(this);

                for (ScanResult scanResult : wifiScanResults) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();

                    // hide secured networks
                    if(scanResult.capabilities.toUpperCase().contains("WPA") ||
                        scanResult.capabilities.toUpperCase().contains("WPE")) {
                        break;
                    }

                    // mark connected network
                    if (ssid.equals("\"" + scanResult.SSID + "\"" )) {
                        wifiResults.add(scanResult.SSID + " ✅");
                        Util.connected();
                    } else {
                        wifiResults.add(scanResult.SSID);
                    }
                    arrayAdapter.notifyDataSetChanged();
                }

                if (wifiResults.size() > 0) {
                    text_msg.setVisibility(View.INVISIBLE);
                    Util.connected();
                } else {
                    Util.disconnected();
                }
            }
        };

        activity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
        String selectedSSID = parent.getItemAtPosition(index).toString();

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String connectedSSID = wifiInfo.getSSID();

        if (connectedSSID.equals("\"" + selectedSSID + "\"")) {
            wifiResults.set(index, selectedSSID + " ✅");
            arrayAdapter.notifyDataSetChanged();
            Toast.makeText(context, "Connecté à " + selectedSSID, Toast.LENGTH_SHORT).show();
        } else if (selectedSSID.contains(" ✅")) {
            Toast.makeText(context, "Déjà Connecté", Toast.LENGTH_SHORT).show();
        } else {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID =  "\"" + selectedSSID + "\"";

            wifiConfig.wepTxKeyIndex = 0;

            // manage wpa & wpe networks
//            if(networkCapabilities.toUpperCase().contains("WEP")) { // WEP Network.
//                wifiConfig.wepKeys[0] = "\"" + networkPass + "\"";
//                wifiConfig.wepTxKeyIndex = 0;
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            } else if(networkCapabilities.toUpperCase().contains("WPA")) { // WPA Network
//                wifiConfig.preSharedKey = "\""+ networkPass +"\"";
//            } else  { // OPEN Network.
//                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            }

            // open network only
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            this.wifiManager.addNetwork(wifiConfig);

            List<WifiConfiguration> list = this.wifiManager.getConfiguredNetworks();
            for( WifiConfiguration config : list ) {
                if (config.SSID != null && config.SSID.equals("\"" + selectedSSID + "\"")) {
                    this.wifiManager.disconnect();
                    this.wifiManager.enableNetwork(config.networkId, true);
                    this.wifiManager.reconnect();
                    Toast.makeText(context, "Connecté à " + selectedSSID, Toast.LENGTH_SHORT).show();
                    wifiResults.set(index, selectedSSID + " ✅");
                    arrayAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
        Util.connected();
    }

    /**
     * Get the available Networks
     * @return retrieves an array list of wifi access points
     */
    public ArrayList<String> getAvailableNetworks(){
        scanWifi();
        return (ArrayList<String>) wifiResults.clone();
    }

    /**
     * Get current network IP address
     * @return In successful case retrieves a string contains the IP address, 0.0.0.0 otherwise
     */
    public String getIpAddress(){
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return Formatter.formatIpAddress(wifiInfo.getIpAddress());
    }
}