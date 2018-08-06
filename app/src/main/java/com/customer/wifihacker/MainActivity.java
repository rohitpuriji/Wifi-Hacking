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

import java.util.List;

public class MainActivity extends ListActivity {
    WifiManager mainWifiObj;
    WifiScanReceiver wifiReciever;
    ListView list;
    String wifis[];
    WifiConnectedReciever mWifiConnectedReciever;
    EditText pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list=getListView();
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

                // selected item
                String ssid = ((TextView) view).getText().toString();
                connectToWifi(ssid);
                Toast.makeText(MainActivity.this,"Wifi SSID : "+ssid,Toast.LENGTH_SHORT).show();

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
            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            Log.e("wifiScanList",wifiScanList.size()+"");

            wifis = new String[wifiScanList.size()];
            for(int i = 0; i < wifiScanList.size(); i++){
                wifis[i] = ((wifiScanList.get(i)).toString());
                Log.e("wifi name",wifis[i]);
            }

            String filtered[] = new String[wifiScanList.size()];
            int counter = 0;
            for (String eachWifi : wifis) {
                String[] temp = eachWifi.split(",");
                filtered[counter] = temp[0].substring(5).trim() ;
                counter++;

            }
            list.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, R.id.label, filtered));


        }
    }

    private void finallyConnect(String networkPass, String networkSSID) {

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + networkSSID + "\"";
        conf.preSharedKey = "\"" + networkPass + "\"";

        conf.status = WifiConfiguration.Status.ENABLED;
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);

        Log.e("wifi networkSSID",networkSSID);
        Log.e("wifi networkPass",networkPass);

        Log.e("connecting", conf.SSID + " " + conf.preSharedKey);
        mainWifiObj.addNetwork(conf);
        Log.e("after connecting", conf.SSID + " " + conf.preSharedKey);

        List<WifiConfiguration> list = mainWifiObj.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {

            Log.e("i.SSID", " :: "+i.SSID);

            if(i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                Log.e("i.networkId", " :: matched :: "+i.networkId);

                mainWifiObj.disconnect();
                mainWifiObj.enableNetwork(i.networkId, true);
                mainWifiObj.reconnect();
                Log.e("re connecting", i.SSID + " " + conf.preSharedKey);
                break;
            }
        }
    }


    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = dialog.findViewById(R.id.textSSID1);

        Button dialogButton = dialog.findViewById(R.id.okButton);
        pass = dialog.findViewById(R.id.textPassword);
        textSSID.setText(wifiSSID);

        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checkPassword = pass.getText().toString();
                finallyConnect(checkPassword, wifiSSID);
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