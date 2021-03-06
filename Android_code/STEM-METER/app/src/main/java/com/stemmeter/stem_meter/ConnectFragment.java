package com.stemmeter.stem_meter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * Created by Josh on 1/28/2017.
 */

public class ConnectFragment extends ListFragment {
    private String TAG = "ConnectFragment";
    ConnectFragInterface connectFragInterface;
    private TextView connectTextView;
    private ScanListAdapter scanListAdapter;
    private GraphFileStorage graphFileStorage;

    // Container Activity must implement this interface
    public interface ConnectFragInterface {
        void BoardConnect(BluetoothDevice device);
        void BLEScan();
        void setSavedGraphDataList(ArrayList<SavedGraphData> savedGraphData);
    }

    @Override
    public void onResume() {
        super.onResume();
        connectFragInterface.BLEScan();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scanListAdapter = new ScanListAdapter();
        setListAdapter(scanListAdapter);
        graphFileStorage = new GraphFileStorage();
        connectFragInterface.setSavedGraphDataList(graphFileStorage.readGraphFiles(getActivity()));
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.connect_fragment, container, false);
        connectTextView = (TextView)view.findViewById(R.id.connectTextView);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            connectFragInterface = (ConnectFragInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConnectFragInterface");
        }
    }

    public void printConnectionStatus(final String string) {
        connectTextView.post(
                new Runnable() {
                    public void run() {
                        connectTextView.setText(string);
                    }
                });
    }

    public void addScanListItem(BLEDevice bleDevice) {
        boolean deviceInList = false;
        if(scanListAdapter.getCount() == 0) {
            scanListAdapter.addItem(bleDevice);
            return;
        }
        else {
            for(int i=0;i<scanListAdapter.getCount();i++) {
                if(bleDevice.getBluetoothDevice().getAddress().equals(scanListAdapter.getItem(i).getBluetoothDevice().getAddress())) {
                    deviceInList = true;
                    scanListAdapter.updateItem(i,bleDevice);
                }
            }
        }
        if(!deviceInList) {
            scanListAdapter.addItem(bleDevice);
        }
    }

    public void clearScanList() {
        scanListAdapter.clearList();
    }


    private class ScanListAdapter extends BaseAdapter {

        private ArrayList<BLEDevice> bleDeviceList = new ArrayList<BLEDevice>();
        private ArrayList<Integer> displayCounter = new ArrayList<>();
        private LayoutInflater mInflater;

        public ScanListAdapter() {
            mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final BLEDevice item) {
            bleDeviceList.add(item);
            displayCounter.add(0);
            notifyDataSetChanged();
        }

        public void clearList( ) {
            bleDeviceList.clear();
            displayCounter.clear();
        }

        public void updateItem(final int index, final BLEDevice item) {
            int previousValue = displayCounter.get(index);
            previousValue++;
            displayCounter.set(index, previousValue);
            if(previousValue > 5) {
                displayCounter.set(index,0);
                bleDeviceList.set(index,item);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return bleDeviceList.size();
        }

        @Override
        public BLEDevice getItem(int position) {
            return bleDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView deviceStrTextView;
            TextView rssiTextView;
            TextView addressTextView;
            final ImageButton connectButton;
            ProgressBar rssiProgressBar;

            if (convertView == null) {
                // if the view is null then inflate the custom item layout
                convertView = mInflater.inflate(R.layout.scan_list_item, null);
            }

            deviceStrTextView = (TextView) convertView.findViewById(R.id.scanItemTextView);
            rssiTextView = (TextView) convertView.findViewById(R.id.scanRSSITextView);
            connectButton = (ImageButton) convertView.findViewById(R.id.connectButton);
            addressTextView = (TextView) convertView.findViewById(R.id.scanAddressTextView);
            rssiProgressBar = (ProgressBar) convertView.findViewById(R.id.rssiProgressBar);

            try {
                // write the string to the text view
                deviceStrTextView.setText(bleDeviceList.get(position).getBluetoothDevice().getName());

                // write the string to the text view
                rssiTextView.setText(bleDeviceList.get(position).getRSSIStr());

                addressTextView.setText(bleDeviceList.get(position).getBluetoothDevice().getAddress());

                rssiProgressBar.setProgress(140+bleDeviceList.get(position).getRssi());
            }
            catch (NullPointerException npe) {
              //  Log.i(TAG,"View Item Null");
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connectFragInterface.BoardConnect(scanListAdapter.getItem(position).getBluetoothDevice());
                }
            });

            connectButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    connectFragInterface.BoardConnect(scanListAdapter.getItem(position).getBluetoothDevice());
                }
            });

            return convertView;
        }
    }
}
