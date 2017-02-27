package com.stemmeter.stem_meter;

import android.app.Activity;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
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

import java.lang.reflect.Array;
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
    private ArrayList<String> dataPointNames;
    private ArrayList<String> unitNames;
    private DataListAdapter dataListAdapter;
    private UnitListAdapter unitListAdapter;
    private ImageButton doneBtn;
    private final String GRAPH_FRAG_TAG = "GraphFragTag";
    // Container Activity must implement this interface

    // Container Activity must implement this interface
    public interface GraphSettingsFragInterface {
        public Sensor getSensor(int sensorNumber);
        //public ArrayList<Boolean> getConnectedSensors();
        public GraphConfig getGraphConfig();
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
                R.array.sensor_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        selectedSensorSpinner.setAdapter(adapter);
        selectedSensorSpinner.setSelection(graphSettingsFragInterface.getGraphConfig().getSelectedSensor());

        // Set the spinner based on its SensorConfig object
        //selectedSensorSpinner.setSelection(sensorFragInterface.getSensorConfig(position+1).getFreq());
        selectedSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int sensorSelected, long id) {
                graphSettingsFragInterface.getGraphConfig().setSelectedSensor(sensorSelected);
                reinializeDataListView();
                reinitializeUnitListView();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        dataListAdapter = new DataListAdapter();
        GraphSettings graphSettings = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings();
        dataPointNames = graphSettings.getDataPoints();
        if (dataPointNames.size() > 0)
        {
            for (int i = 0; i < dataPointNames.size(); i++)
                dataListAdapter.addItem(dataPointNames.get(i));
        }

        ListView dataNameListView = (ListView) view.findViewById(R.id.SelectedSensorDatalist);
        dataNameListView.setAdapter(dataListAdapter);

        unitListAdapter = new UnitListAdapter();
        unitNames = graphSettings.getUnits();
        if (unitNames.size() > 0)
        {
            for (int i = 0; i < unitNames.size(); i++)
                unitListAdapter.addItem(unitNames.get(i));
        }

        ListView unitNameListView = (ListView) view.findViewById(R.id.Unitlist);
        unitNameListView.setAdapter(unitListAdapter);

        doneBtn = (ImageButton) view.findViewById(R.id.DoneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                GraphFragment graphFragment = new GraphFragment();
                transaction.replace(R.id.fragment_container, graphFragment, GRAPH_FRAG_TAG);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }

    private void reinializeDataListView()
    {
        dataListAdapter.removeAllItems();
        GraphSettings graphSettings = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings();
        dataPointNames = graphSettings.getDataPoints();
        if (dataPointNames.size() > 0)
        {
            for (int i = 0; i < dataPointNames.size(); i++)
                dataListAdapter.addItem(dataPointNames.get(i));
        }
    }

    private void reinitializeUnitListView()
    {
        unitListAdapter.removeAllItems();
        GraphSettings graphSettings = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings();
        unitNames = graphSettings.getUnits();
        if (unitNames.size() > 0)
        {
            for (int i = 0; i < unitNames.size(); i++)
                unitListAdapter.addItem(unitNames.get(i));
        }
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

    private class DataListAdapter extends BaseAdapter {
        private ArrayList<String> dataPointName = new ArrayList<String>();
        private LayoutInflater mInflater;
        private String TAG = "CustomAdapter";
        private int currentSelectedPosition;
        private int selectedPosition = -1;

        public DataListAdapter() {

            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final String item) {
            dataPointName.add(item);
            notifyDataSetChanged();
        }

        public void updateItem(final String item, int position) {
            dataPointName.set(position, item);

        }

        public void removeAllItems()
        {
            dataPointName.clear();
        }

        @Override
        public int getCount() {
            return dataPointName.size();
        }

        @Override
        public String getItem(int position) {
            return dataPointName.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView dataNameText;
            final ImageButton deleteBtn;

            if (convertView == null) {
                // if the view is null then inflate the custom item layout
                convertView = mInflater.inflate(R.layout.data_name_list_item, null);
            }

            dataNameText = (TextView) convertView.findViewById(R.id.dataNameItemTextView);

            if (dataNameText != null)
                dataNameText.setText(dataPointName.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });

            return convertView;

        }
    }

    private class UnitListAdapter extends BaseAdapter {
        private ArrayList<String> unitName = new ArrayList<String>();
        private LayoutInflater mInflater;
        private String TAG = "CustomAdapter";
        private int currentSelectedPosition;
        private int selectedPosition = -1;

        public UnitListAdapter() {

            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final String item) {
            unitName.add(item);
            notifyDataSetChanged();
        }

        public void removeAllItems()
        {
            unitName.clear();
        }

        public void updateItem(final String item, int position) {
            unitName.set(position, item);

        }

        @Override
        public int getCount() {
            return unitName.size();
        }

        @Override
        public String getItem(int position) {
            return unitName.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final TextView unitNameText;
            final ImageButton deleteBtn;

            if (convertView == null) {
                // if the view is null then inflate the custom item layout
                convertView = mInflater.inflate(R.layout.unit_name_list_item, null);
            }

            unitNameText = (TextView) convertView.findViewById(R.id.unitNameItemTextView);

            if (unitNameText != null)
                unitNameText.setText(unitName.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });

            return convertView;

        }
    }
}
