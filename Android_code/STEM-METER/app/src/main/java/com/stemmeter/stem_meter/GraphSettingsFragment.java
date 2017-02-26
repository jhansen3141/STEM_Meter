package com.stemmeter.stem_meter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.stemmeter.stem_meter.Sensors.Sensor;

import java.util.ArrayList;
import java.util.List;

import static com.stemmeter.stem_meter.R.id.chart;

/**
 * Created by monro on 2/25/2017.
 */

public class GraphSettingsFragment extends Fragment {

    GraphSettingsFragInterface graphSettingsFragInterface;
    private String TAG = "GraphSettingsFragTag";
    private Spinner selectedSensorSpinner;
    // Container Activity must implement this interface

    // Container Activity must implement this interface
    public interface GraphSettingsFragInterface {
        public Sensor getSensor(int sensorNumber);
    }

    GraphSettingsFragInterface graphSettingsInterface;



    //@Override
    //public void onActivityCreated(Bundle savedInstanceState) {
    //    super.onActivityCreated(savedInstanceState);
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.graph_settings_fragment, container, false);
        selectedSensorSpinner = (Spinner) view.findViewById(R.id.SelectedSensorSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        selectedSensorSpinner.setAdapter(adapter);


        // Set the spinner based on its SensorConfig object
        //selectedSensorSpinner.setSelection(sensorFragInterface.getSensorConfig(position+1).getFreq());
        selectedSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int freqSelected, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            graphSettingsFragInterface = (GraphSettingsFragment.GraphSettingsFragInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GraphSettingsFragInterface");
        }
    }
}
