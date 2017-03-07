package com.stemmeter.stem_meter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.stemmeter.stem_meter.Sensors.Sensor;

import java.util.ArrayList;

/**
 * Created by Josh on 11/28/2016.
 */

public class SensorsFragment extends ListFragment {
    SensorFragInterface sensorFragInterface;
    private String TAG = "SensorFrag";
    private SensorListAdapter sensorListAdapter;
    private ImageButton zeroButton;
    private int listItemSelected = 0;


    // Container Activity must implement this interface
    public interface SensorFragInterface {
        boolean sensorConfigWrite(SensorConfig config);
        SensorConfig getSensorConfig(int sensorNumber);
        GraphConfig getGraphConfig();
        boolean updateBaseUnitTime();
        void readSensorConfigData();
        Sensor getSensor(int sensorNumber);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sensorListAdapter = new SensorListAdapter();

        // Add the four sensors with no data strings to start
        sensorListAdapter.addItem("Sensor 1 - No Data");
        sensorListAdapter.addItem("Sensor 2 - No Data");
        sensorListAdapter.addItem("Sensor 3 - No Data");
        sensorListAdapter.addItem("Sensor 4 - No Data");
        setListAdapter(sensorListAdapter);

        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sensors_fragment, container, false);
        zeroButton = (ImageButton) view.findViewById(R.id.ZeroButton);

        zeroButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Log.i(TAG, "Zero button pressed " + listItemSelected);
                try {
                    sensorFragInterface.getSensor(listItemSelected + 1).zeroSensor();
                }
                catch (NullPointerException npe) {
                    Log.i(TAG,"Sensor Null - Cannot Zero");
                }

            }
        });

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
        // Only read the config settings from the base unit once

        // Update the base unit time
        sensorFragInterface.updateBaseUnitTime();
        // Read the current sensor config settings from base unit
        sensorFragInterface.readSensorConfigData();

    }

    private class SensorListAdapter extends BaseAdapter {

        private ArrayList<String> sensorData = new ArrayList<String>();
        private LayoutInflater mInflater;
        private final ArrayList<SetBoolean> setBooleanList = new ArrayList<SetBoolean>();
        private String TAG = " SensorListAdapter";
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
                View v = null;
                try {
                    // Only update the row we need to. Not the entire list
                    v = getListView().getChildAt(position - getListView().getFirstVisiblePosition());
                }
                catch (Exception e)
                {

                }

                if(v == null) {
                    return;
                }

                TextView sensorText = (TextView) v.findViewById(R.id.sensorDataTextView);

                if(sensorText != null) {
                    sensorText.setText(item);
                }
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
                    public void onClick(View arg0) {
                        if (sensorFragInterface.getGraphConfig().getSelectedSensor() != position) {
                            sensorFragInterface.getGraphConfig().setSelectedSensor(position);
                            listItemSelected = position;
                        }
                        notifyDataSetChanged();
                    }
                });

                if (position == sensorFragInterface.getGraphConfig().getSelectedSensor())
                    convertView.setBackgroundColor(SensorConst.SELECTION_COLOR);

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
                    if(sensorFragInterface.getSensorConfig(finalPosition+1).getFreq() == SensorConst.RATE_OFF) {
                        sensorText.setText("Sensor " + (finalPosition+1) + " - No Data");
                    }
                    else {
                        // write the string to the text view
                        sensorText.setText(sensorData.get(position));
                    }
                }
            }
            else {
                ListButton = (ImageButton) altView.findViewById(R.id.ListBtn);
                sdCheck = (CheckedTextView) altView.findViewById(R.id.checkedTextViewSD);
                frequencySpinner = (Spinner) altView.findViewById(R.id.FrequencySpinner);
                altView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (sensorFragInterface.getGraphConfig().getSelectedSensor() != position) {
                            sensorFragInterface.getGraphConfig().setSelectedSensor(position);
                            listItemSelected = position;
                        }
                        notifyDataSetChanged();
                    }
                    });

                if (position == sensorFragInterface.getGraphConfig().getSelectedSensor()) {
                    altView.setBackgroundColor(SensorConst.SELECTION_COLOR);
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

                // Get the selected freq from spinner and send it to base unit
                frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int freqSelected, long id)
                    {
                        // Create a new config object
                        SensorConfig config = new SensorConfig(finalPosition+1);
                        // Set the freq to the one just selected
                        config.setFreq(freqSelected);
                        // Set the SD logging boolean to whatever it was before
                        config.setSDLogging(sensorFragInterface.getSensorConfig(finalPosition + 1).isSDLogging());
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
                        // Create a new config object
                        // Add one to offset zero based number
                        SensorConfig config = new SensorConfig(finalPosition+1);
                        // Set the freq to what it was before
                        config.setFreq(sensorFragInterface.getSensorConfig(finalPosition+1).getFreq());

                        // if it was checked then now its not
                        if (sdCheck.isChecked()) {
                            sdCheck.setChecked(false);
                            // Set the SD logging boolean to false
                            config.setSDLogging(false);

                        }
                        // if it wasnt checked now it is
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
