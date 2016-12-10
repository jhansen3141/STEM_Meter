package com.example.josh.boardtestx;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Josh on 11/28/2016.
 */

public class SensorsFragment extends Fragment {
    SensorFragInterface sensorFragInterface;
    private String TAG = "SensorFrag";
    private TextView sensor1TextView;
    private TextView sensor2TextView;
    private TextView sensor3TextView;
    private TextView sensor4TextView;
    private Button ledButton;
    private boolean ledON = false;

    // Container Activity must implement this interface
    public interface SensorFragInterface {
        public void ledToggle(boolean ledON);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.sensors_fragment, container, false);
        sensor1TextView = (TextView)view.findViewById(R.id.sensor1TextView);
        sensor2TextView = (TextView)view.findViewById(R.id.sensor2TextView);
        sensor3TextView = (TextView)view.findViewById(R.id.sensor3TextView);
        sensor4TextView = (TextView)view.findViewById(R.id.sensor4TextView);
        ledButton = (Button)view.findViewById(R.id.ledButton);

        ledButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG,"LED button pressed");

                new Thread(new Runnable() {
                    public void run() {
                        if(ledON) {
                            ledON = false;
                            ledButton.post(
                                    new Runnable() {
                                        public void run() {
                                            ledButton.setBackgroundColor(Color.LTGRAY);
                                            ledButton.setText("LED ON");
                                        }
                                    });
                            sensorFragInterface.ledToggle(!ledON);
                        }
                        else {
                            ledON = true;
                            ledButton.post(
                                    new Runnable() {
                                        public void run() {
                                            ledButton.setBackgroundColor(Color.GREEN);
                                            ledButton.setText("LED OFF");
                                        }
                                    });

                            sensorFragInterface.ledToggle(!ledON);
                        }
                    }
                }).start();

            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            sensorFragInterface = (SensorsFragment.SensorFragInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SensorFragInterface");
        }
    }

    public void printSensorData(int sensorNum, final String dataStr) {
        switch (sensorNum) {
            case SensorList.SENSOR_1:
                if(sensor1TextView != null) {
                    sensor1TextView.post(
                            new Runnable() {
                                public void run() {
                                    sensor1TextView.setText(dataStr);
                                }
                            });
                }
                break;
            case SensorList.SENSOR_2:
                if(sensor2TextView != null) {
                    sensor2TextView.post(
                            new Runnable() {
                                public void run() {
                                    sensor2TextView.setText(dataStr);
                                }
                            });
                }
                break;
            case SensorList.SENSOR_3:
                if(sensor3TextView != null) {
                    sensor3TextView.post(
                            new Runnable() {
                                public void run() {
                                    sensor3TextView.setText(dataStr);
                                }
                            });
                }
                break;
            case SensorList.SENSOR_4:
                if(sensor4TextView != null) {
                    sensor4TextView.post(
                            new Runnable() {
                                public void run() {
                                    sensor4TextView.setText(dataStr);
                                }
                            });
                }
                break;

        }
    }
}
