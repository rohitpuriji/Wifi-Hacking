package com.customer.wifihacker;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiListAdapter extends BaseAdapter {

    private ArrayList<ScanResult> mScanResults;
    private LayoutInflater inflter;
    private Context context;

    WifiListAdapter(ArrayList<ScanResult> wifiScanList,Context applicationContext){
        mScanResults = wifiScanList;
        context =applicationContext;
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return mScanResults.size();
    }

    @Override
    public Object getItem(int i) {
        return mScanResults.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.list_item, null);
        TextView label = view.findViewById(R.id.label);
        TextView bssid = view.findViewById(R.id.bssid);
        TextView securitytype = view.findViewById(R.id.securitytype);
        TextView range = view.findViewById(R.id.range);
        TextView other = view.findViewById(R.id.other);

        label.setText(mScanResults.get(i).SSID);
        bssid.setText(mScanResults.get(i).BSSID);
        securitytype.setText(mScanResults.get(i).capabilities);

        int freq = mScanResults.get(i).frequency / 100;

        range.setText(freq+" GHz");



        int level = mScanResults.get(i).level;

        if (level <= 0 && level >= -50) {
            other.setText("Best");
        } else if (level < -50 && level >= -70) {
            other.setText("Good");
        } else if (level < -70 && level >= -80) {
            other.setText("Low");
        } else if (level < -80 && level >= -100) {
            other.setText("Very Weak");
        } else {
            other.setText("No Available");
        }


        return view;
    }
}
