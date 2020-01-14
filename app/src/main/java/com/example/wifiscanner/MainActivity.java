package com.example.wifiscanner;


import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //    TextView mainText;
    public ArrayList<String> devices = new ArrayList<>();
    TextView welcomeText;
    ListView listView;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    StringBuilder csv = new StringBuilder();
    boolean scanFinished = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        mainText = (TextView) findViewById(R.id.mainText);
        welcomeText = (TextView) findViewById(R.id.welcomeText);
        listView = (ListView) findViewById(R.id.deviceList);
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        welcomeText.setText("Starting Scan...\n");

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        menu.add(0, 1, 1, "Finish");
        return super.onCreateOptionsMenu(menu);
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiverWifi);
        Intent scanResults = new Intent();
        scanResults.putExtra("AP_LIST", csv.toString());
        setResult(RESULT_OK, scanResults);
        finish();
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(receiverWifi, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public static class DialogMessage extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String ssid = getArguments().getString("ssid");
            String bssid = getArguments().getString("bssid");
            String capabilities = getArguments().getString("capabilities");
            String frequency = "" + getArguments().getInt("frequency");
            String level = "" + getArguments().getInt("level");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(ssid).setItems(new CharSequence[]{ssid, bssid, capabilities, frequency, level}, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            })
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

            return builder.create();
        }
    }

    public class WifiInformation {
        public String ssid;
        public String bssid;
        public String capabilities;
        public int frequency;
        public int level;

        WifiInformation(String ssid, String bssid, String capabilities, int frequency, int level) {
            this.ssid = ssid;
            this.bssid = bssid;
            this.capabilities = capabilities;
            this.frequency = frequency;
            this.level = level;
        }
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            sb = new StringBuilder();
            csv = new StringBuilder();
            wifiList = mainWifi.getScanResults();

            // prepare text for display and CSV table

            sb.append("Number of APs Detected: ");
            sb.append((Integer.valueOf(wifiList.size())).toString());
            sb.append("\n\n");
            final HashMap<String, WifiInformation> dict = new HashMap<>();


            if (wifiList.size() > 0) {
                listView = (ListView) findViewById(R.id.deviceList);
                for (int i = 0; i < wifiList.size(); i++) {
                    devices.add(wifiList.get(i).SSID);
                    dict.put(wifiList.get(i).SSID, new WifiInformation(wifiList.get(i).SSID, wifiList.get(i).BSSID, wifiList.get(i).capabilities, wifiList.get(i).frequency, wifiList.get(i).level));
                }
                ArrayAdapter arrayAdapter = new ArrayAdapter(c, android.R.layout.simple_list_item_1, devices);
                listView.setAdapter(arrayAdapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView parent, View view, int position, long id) {
                        String listItem = listView.getItemAtPosition(position).toString();
                        DialogFragment d = new DialogMessage();
                        Bundle args = new Bundle();
                        args.putString("ssid", dict.get(listItem).ssid);
                        args.putString("bssid", dict.get(listItem).bssid);
                        args.putString("capabilities", dict.get(listItem).capabilities);
                        args.putInt("frequency", dict.get(listItem).frequency);
                        args.putInt("level", dict.get(listItem).level);
                        d.setArguments(args);
                        d.show(getSupportFragmentManager(), "Hello" + id);
                    }
                });
            }
            welcomeText.setText(sb);
            scanFinished = true;
        }
    }
}
