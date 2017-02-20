package com.stemmeter.stem_meter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
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
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class SensorsFragment extends ListFragment {
    SensorFragInterface sensorFragInterface;
    private String TAG = "SensorFrag";
    private SensorListAdapter sensorListAdapter;

    //private SensorConfig s1Config = new SensorConfig();



    // Container Activity must implement this interface
    public interface SensorFragInterface {
        public void sensorConfigWrite(int sensorNumber, int sensorRate);
        public SensorConfig getSensorConfig(int sensorNumber);
        public GraphConfig getGraphConfig();
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

        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //registerForContextMenu(getListView());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sensors_fragment, container, false);
        return view;
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        MenuInflater inflater = getActivity().getMenuInflater();
//        inflater.inflate(R.menu.sensor_freq_popup, menu);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//        // get the sensor number (sensors 1 is added first, then sensor 2...)
//        int sensorNumber = (int)info.id + 1; // Sensor 1 ID = 1 so add one to offset base 0
//        int sensorRate;
//        boolean returnState = true;
//        // set the rate to write to the base unit
//        switch (item.getItemId()) {
//            case R.id.off:
//                sensorRate = SensorList.RATE_OFF;
//                break;
//            case R.id.tenhz:
//                sensorRate = SensorList.RATE_TEN_HZ;
//                break;
//            case R.id.fivehz:
//                sensorRate = SensorList.RATE_FIVE_HZ;
//                break;
//            case R.id.onehz:
//                sensorRate = SensorList.RATE_ONE_HZ;
//                break;
//            case R.id.onemin:
//                sensorRate = SensorList.RATE_ONE_MIN;
//                break;
//            case R.id.tenmin:
//                sensorRate = SensorList.RATE_TEN_MIN;
//                break;
//            case R.id.thirtymin:
//                sensorRate = SensorList.RATE_THRITY_MIN;
//                break;
//            case R.id.onehour:
//                sensorRate = SensorList.RATE_ONE_HOUR;
//                break;
//            default:
//               sensorRate = -1;
//                returnState =  super.onContextItemSelected(item);
//        }
//        if(sensorRate >= 0) {
//            // if a valid rate was assigned then write it to the base station
//            // BLE ops happen in the main activity so use the interface
//            sensorFragInterface.sensorConfigWrite(sensorNumber,sensorRate);
//        }
//        return returnState;
//    }

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
        private final ArrayList<SetBoolean> setBooleanList = new ArrayList<SetBoolean>();
        private String TAG = "CustomAdapter";
        //private int currentSelectedPosition;

        public SensorListAdapter() {
            setBooleanList.add(new SetBoolean());
            setBooleanList.add(new SetBoolean());
            setBooleanList.add(new SetBoolean());
            setBooleanList.add(new SetBoolean());

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView sensorText;
            final ImageButton settingsButton;
            final ImageButton ListButton;
            final CheckedTextView sdCheck;
            final Spinner frequencySpinner;
            final int finalPosition = position;

            final  View altView  = mInflater.inflate(R.layout.sensor_list_config_item, null);
            if (convertView == null) {
                // if the view is null then inflate the custom item layout
                convertView = mInflater.inflate(R.layout.sensor_list_item, null);
            }

            if(setBooleanList.get(finalPosition).isSet()) {
                convertView = mInflater.inflate(R.layout.sensor_list_item, null);

                convertView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0)
                    {
                        if (sensorFragInterface.getGraphConfig().getSelectedSensor() != position)
                            sensorFragInterface.getGraphConfig().setSelectedSensor(position);
                        notifyDataSetChanged();
                    }
                });

                if (position == sensorFragInterface.getGraphConfig().getSelectedSensor())
                    convertView.setBackgroundColor(Color.BLUE);

                // get the text view in the layout
                sensorText = (TextView) convertView.findViewById(R.id.sensorDataTextView);
                settingsButton = (ImageButton)convertView.findViewById(R.id.SettingsBtn);

                settingsButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        setBooleanList.get(finalPosition).setSet(false);
                        notifyDataSetChanged();
                    }
                });

                if(sensorText != null) {
                    // write the string to the text view
                    sensorText.setText(sensorData.get(position));
                }
            }
            else {
                ListButton = (ImageButton) altView.findViewById(R.id.ListBtn);
                sdCheck = (CheckedTextView) altView.findViewById(R.id.checkedTextViewSD);
                frequencySpinner = (Spinner) altView.findViewById(R.id.FrequencySpinner);
                altView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0)
                    {
                        if (sensorFragInterface.getGraphConfig().getSelectedSensor() != position)
                            sensorFragInterface.getGraphConfig().setSelectedSensor(position);
                        notifyDataSetChanged();
                    }
                    });

                if (position == sensorFragInterface.getGraphConfig().getSelectedSensor())
                    altView.setBackgroundColor(Color.BLUE);

                sdCheck.setChecked(sensorFragInterface.getSensorConfig(position+1).isSDLogging());


                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.frequency_array, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                frequencySpinner.setAdapter(adapter);
                frequencySpinner.setSelection(sensorFragInterface.getSensorConfig(position+1).getFreq());
                frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        Log.i(TAG,"P:" + position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });


                // Set listener to listen for when check mark is clicked for check mark in list item settings SD card
                sdCheck.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Log.i(TAG, "SD Checkbox clicked");
                        if (sdCheck.isChecked())
                            sdCheck.setChecked(false);
                        else
                            sdCheck.setChecked(true);
                    }
                    });

                // Set listener to listen for when List button is clicked in list item settiings view
                ListButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        setBooleanList.get(finalPosition).setSet(true);
                        notifyDataSetChanged();
                    }
                });

            }

            if(setBooleanList.get(finalPosition).isSet()) {
                Log.i(TAG,"Returning Normal View");
                return convertView;
            }
            else {
                Log.i(TAG,"Returning Alt View");
                return altView;
            }

        }

        public class SetBoolean {
            private boolean isSet = true;

            public boolean isSet() {
                return isSet;
            }

            public void setSet(boolean set) {
                isSet = set;
            }
        }


    }
}
