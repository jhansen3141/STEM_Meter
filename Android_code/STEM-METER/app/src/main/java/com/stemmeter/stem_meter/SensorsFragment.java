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

    // Container Activity must implement this interface
    public interface SensorFragInterface {
        boolean sensorConfigWrite(SensorConfig config);
        SensorConfig getSensorConfig(int sensorNumber);
        GraphConfig getGraphConfig();
        boolean updateBaseUnitTime();
        void readSensorConfigData();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sensors_fragment, container, false);
        return view;
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
                sensorListAdapter.updateItem(dataStr, sensorNum - 1);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorFragInterface.updateBaseUnitTime();
        sensorFragInterface.readSensorConfigData();
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
            // Only update the sensor data if sensor text box is showing
            if(setBooleanList.get(position).isSet()) {
                sensorData.set(position, item);
                this.notifyDataSetChanged();
            }
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

                if (position == sensorFragInterface.getGraphConfig().getSelectedSensor()) {
                    altView.setBackgroundColor(Color.BLUE);
                }

                // set the SD card check box based on its SensorConfig object
                sdCheck.setChecked(sensorFragInterface.getSensorConfig(position+1).isSDLogging());


                // Create an ArrayAdapter using the string array and a default spinner layout
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                        R.array.frequency_array, android.R.layout.simple_spinner_item);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                frequencySpinner.setAdapter(adapter);
                // Set the spinner based on its SensorConfig object
                frequencySpinner.setSelection(sensorFragInterface.getSensorConfig(position+1).getFreq());
                frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int freqSelected, long id)
                    {
                        // Create a new config object
                        SensorConfig config = new SensorConfig(finalPosition+1);
                        // Set the freq to the one just selected
                        config.setFreq(freqSelected);
                        // Set the SD logging boolean to whatever it was before
                        config.setSDLogging(sensorFragInterface.getSensorConfig(finalPosition+1).isSDLogging());
                        // Write the new config to the base unit over BLE
                        sensorFragInterface.sensorConfigWrite(config);
                        Log.i(TAG,"FS:" + freqSelected);
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
                        // Create a new config object
                        SensorConfig config = new SensorConfig(finalPosition+1);
                        // Set the freq to what it was before
                        config.setFreq(sensorFragInterface.getSensorConfig(finalPosition+1).getFreq());

                        if (sdCheck.isChecked()) {
                            sdCheck.setChecked(false);
                            // Set the SD logging boolean to false
                            config.setSDLogging(false);

                        }
                        else {
                            sdCheck.setChecked(true);
                            // Set the SD logging boolean to false
                            config.setSDLogging(true);
                        }

                        // Write the new config to the base unit over BLE
                        sensorFragInterface.sensorConfigWrite(config);
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
               // Log.i(TAG,"Returning Normal View");
                return convertView;
            }
            else {
              //  Log.i(TAG,"Returning Alt View");
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
