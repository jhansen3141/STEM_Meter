package com.example.josh.boardtestx;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class SensorsFragment extends ListFragment {
    SensorFragInterface sensorFragInterface;
    private String TAG = "SensorFrag";
    private SensorListAdapter sensorListAdapter;

    // Container Activity must implement this interface
    public interface SensorFragInterface {
        public void sensorConfigWrite(int sensorNumber, int sensorRate);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sensorListAdapter = new SensorListAdapter();
        // Add the four sensors with disconnected strings to start
        sensorListAdapter.addItem("Sensor 1 Disconnected");
        sensorListAdapter.addItem("Sensor 2 Disconnected");
        sensorListAdapter.addItem("Sensor 3 Disconnected");
        sensorListAdapter.addItem("Sensor 4 Disconnected");
        setListAdapter(sensorListAdapter);
        registerForContextMenu(getListView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sensors_fragment, container, false);
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sensor_freq_popup, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // get the sensor number (sensors 1 is added first, then sensor 2...)
        int sensorNumber = (int)info.id + 1; // Sensor 1 ID = 1 so add one to offset base 0
        int sensorRate;
        boolean returnState = true;
        // set the rate to write to the base unit
        switch (item.getItemId()) {
            case R.id.off:
                sensorRate = SensorList.RATE_OFF;
                break;
            case R.id.tenhz:
                sensorRate = SensorList.RATE_TEN_HZ;
                break;
            case R.id.fivehz:
                sensorRate = SensorList.RATE_FIVE_HZ;
                break;
            case R.id.onehz:
                sensorRate = SensorList.RATE_ONE_HZ;
                break;
            case R.id.onemin:
                sensorRate = SensorList.RATE_ONE_MIN;
                break;
            case R.id.tenmin:
                sensorRate = SensorList.RATE_TEN_MIN;
                break;
            case R.id.thirtymin:
                sensorRate = SensorList.RATE_THRITY_MIN;
                break;
            case R.id.onehour:
                sensorRate = SensorList.RATE_ONE_HOUR;
                break;
            default:
               sensorRate = -1;
                returnState =  super.onContextItemSelected(item);
        }
        if(sensorRate >= 0) {
            // if a valid rate was assigned then write it to the base station
            // BLE ops happen in the main activity so use the interface
            sensorFragInterface.sensorConfigWrite(sensorNumber,sensorRate);
        }
        return returnState;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            // assign the interface object
            sensorFragInterface = (SensorsFragment.SensorFragInterface) activity;
        } catch (ClassCastException e) {
            // throw exception if the interface is not implemented
            throw new ClassCastException(activity.toString()
                    + " must implement SensorFragInterface");
        }
    }

    public void printSensorData(final int sensorNum, final String dataStr) {
        // update the item in the list view
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sensorListAdapter.updateItem(dataStr,sensorNum-1);
                sensorListAdapter.notifyDataSetChanged();
            }
        });

    }

    private class SensorListAdapter extends BaseAdapter {

        private ArrayList<String> sensorData = new ArrayList<String>();
        private LayoutInflater mInflater;

        public SensorListAdapter() {
            mInflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final String item) {
            sensorData.add(item);
            notifyDataSetChanged();
        }

        public void updateItem(final String item, int position) {
            sensorData.set(position,item);

        }

        @Override
        public int getCount() {
            return sensorData.size();
        }

        @Override
        public String getItem(int position) {
            return sensorData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView sensorText;
            if (convertView == null) {
                // if the view is null then inflate the custom item layout
                convertView = mInflater.inflate(R.layout.sensor_list_item, null);
            }
            // get the text view in the layout
            sensorText = (TextView) convertView.findViewById(R.id.sensorDataTextView);
            if(sensorText != null) {
                // write the string to the text view
                sensorText.setText(sensorData.get(position));
            }
            return convertView;
        }
    }
}
