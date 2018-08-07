package com.customer.wifihacker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager mainWifiObj;
    private WifiScanReceiver wifiReciever;
    private ListView list;
    private WifiConnectedReciever mWifiConnectedReciever;
    private EditText pass;
    private ArrayList<ScanResult> wifiScanList;
    private WifiListAdapter mWifiListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.list);
        mainWifiObj = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        mWifiConnectedReciever = new WifiConnectedReciever();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},100);
        }else{
            mainWifiObj.startScan();
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectToWifi(position);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 100) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            mainWifiObj.startScan();
        }
    }

    protected void onPause() {
        unregisterReceiver(wifiReciever);
        unregisterReceiver(mWifiConnectedReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(mWifiConnectedReciever, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        super.onResume();
    }


    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {

            wifiScanList= (ArrayList<ScanResult>)mainWifiObj.getScanResults();
            Log.e("wifiScanList",wifiScanList.size()+"");

            for(int i = 0; i < wifiScanList.size(); i++){
                Log.e("wifi name",wifiScanList.get(i).SSID);
                Log.e("wifi name",wifiScanList.get(i).capabilities);
            }

            mWifiListAdapter = new WifiListAdapter(wifiScanList,MainActivity.this);
            list.setAdapter(mWifiListAdapter);

        }
    }

    private void finallyConnect(String networkPass, int pos) {

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + wifiScanList.get(pos).SSID + "\"";

        if (wifiScanList.get(pos).capabilities.toUpperCase().contains("WEP")) {
            Log.e("WEP", "Configuring WEP");
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

            if (networkPass.matches("^[0-9a-fA-F]+$")) {
                conf.wepKeys[0] = networkPass;
            } else {
                conf.wepKeys[0] = "\"".concat(networkPass).concat("\"");
            }

            conf.wepTxKeyIndex = 0;

        } else if (wifiScanList.get(pos).capabilities.toUpperCase().contains("WPA")) {
            Log.e("WPA", "Configuring WPA");

            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

            conf.preSharedKey = "\"" + networkPass + "\"";

        }else {
            Log.e("OPEN", "Configuring OPEN network");
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            conf.allowedAuthAlgorithms.clear();
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        }

        Log.e("wifi networkSSID",wifiScanList.get(pos).SSID);
        Log.e("wifi networkPass",networkPass);


        mainWifiObj.addNetwork(conf);


        List<WifiConfiguration> list = mainWifiObj.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {

            Log.e("i.SSID", " :: "+i.SSID);

            if(i.SSID != null && i.SSID.equals("\"" + wifiScanList.get(pos).SSID + "\"")) {
                Log.e("i.networkId", " :: matched :: "+i.networkId);

                mainWifiObj.disconnect();
                mainWifiObj.enableNetwork(i.networkId, true);
                mainWifiObj.reconnect();
                Log.e("re connecting", i.SSID + " " + conf.preSharedKey);
                break;
            }
        }
    }


    private void connectToWifi(final int pos) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = dialog.findViewById(R.id.textSSID1);

        Button dialogButton = dialog.findViewById(R.id.okButton);
        pass = dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiScanList.get(pos).SSID);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPassword = pass.getText().toString();
                finallyConnect(checkPassword, pos);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private class WifiConnectedReciever extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                boolean connected = info.isConnected();
                if (connected)
                Toast.makeText(getApplicationContext(),"SuccessFully Connected...",Toast.LENGTH_LONG).show();

            }
        }
    }
}