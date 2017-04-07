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
import android.widget.SeekBar;
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

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static com.stemmeter.stem_meter.R.id.chart;
import static com.stemmeter.stem_meter.R.id.seekBar;

/**
 * Created by monro on 2/25/2017.
 */

public class GraphSettingsFragment extends Fragment {

    GraphSettingsFragInterface graphSettingsFragInterface;
    private String TAG = "GraphSettingsFragTag";
    private Spinner selectedSensorSpinner;
    private ArrayList<String> dataPointNames;
    private ArrayList<String> unitNames1;
    private ArrayList<String> unitNames2;
    private DataListAdapter dataListAdapter;
    private UnitList1Adapter unitList1Adapter;
    private UnitList2Adapter unitList2Adapter;
    private ImageButton doneBtn;
    private ImageButton cancelBtn;
    private SeekBar dataSeekBar;
    private final String GRAPH_FRAG_TAG = "GraphFragTag";
    private TextView seekBarText;
    private ArrayList<Boolean> dataPoints;
    private int selectedUnitPosition1;
    private int selectedUnitPosition2;
    ListView unitNameListView2;
    // Container Activity must implement this interface

    // Container Activity must implement this interface
    public interface GraphSettingsFragInterface {
        public Sensor getSensor(int sensorNumber);
        //public ArrayList<Boolean> getConnectedSensors();
        public GraphConfig getGraphConfig();
    }

    //GraphSettingsFragInterface graphSettingsInterface;



    //@Override
    //public void onActivityCreated(Bundle savedInstanceState) {
    //    super.onActivityCreated(savedInstanceState);
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.graph_settings_fragment, container, false);

        ArrayList<String> connectedSensors = new ArrayList<String>();
        if (graphSettingsFragInterface.getSensor(1) != null)
            connectedSensors.add(graphSettingsFragInterface.getSensor(1).getSensorName() + " Sensor");
        if (graphSettingsFragInterface.getSensor(2) != null)
            connectedSensors.add(graphSettingsFragInterface.getSensor(2).getSensorName() + " Sensor");
        if (graphSettingsFragInterface.getSensor(3) != null)
            connectedSensors.add(graphSettingsFragInterface.getSensor(3).getSensorName() + " Sensor");
        if (graphSettingsFragInterface.getSensor(4) != null)
            connectedSensors.add(graphSettingsFragInterface.getSensor(4).getSensorName() + " Sensor");

        ArrayAdapter<String> connectedSensorNames = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, connectedSensors);

        selectedSensorSpinner = (Spinner) view.findViewById(R.id.SelectedSensorSpinner);
        // Specify the layout to use when the list of choices appears
        connectedSensorNames.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        selectedSensorSpinner.setAdapter(connectedSensorNames);
        selectedSensorSpinner.setSelection(graphSettingsFragInterface.getGraphConfig().getSelectedSensor());

        // Set the spinner based on its SensorConfig object
        //selectedSensorSpinner.setSelection(sensorFragInterface.getSensorConfig(position+1).getFreq());
        selectedSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View viewd,
                                       int sensorSelected, long id) {
                if (graphSettingsFragInterface.getSensor(sensorSelected + 1) == null) {
                    selectedSensorSpinner.setSelection(graphSettingsFragInterface.getGraphConfig().getSelectedSensor());
                    return;
                }

                if (graphSettingsFragInterface.getGraphConfig().getSelectedSensor() == sensorSelected)
                    return;

                graphSettingsFragInterface.getGraphConfig().setSelectedSensor(sensorSelected);
                reinitializeDataListView();
                reinitializeUnit1ListView();

                TextView unitNameTextView2 = (TextView) view.findViewById(R.id.SelectedUnitText2);
                TextView unitNameTextView1 = (TextView) view.findViewById(R.id.SelectedUnitText1);
                String dataName1Text = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getSensorName() + " Units";
                unitNameTextView1.setText(dataName1Text);

                GraphSettings graphSettings = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings();
                graphSettingsFragInterface.getGraphConfig().setSelectedUnitsPosition1(0);
                selectedUnitPosition1 = 0;

                if (graphSettings.sensorHasUniqueDataSetUnits()) {
                    reinitializeUnit2ListView();
                    unitNameListView2.setVisibility(View.VISIBLE);
                    unitNameTextView2.setVisibility(View.VISIBLE);
                    String dataName2Text = dataPointNames.get(1) + " Units";
                    unitNameTextView2.setText(dataName2Text);
                    graphSettingsFragInterface.getGraphConfig().setSelectedUnitsPosition2(0);
                    selectedUnitPosition2 = 0;
                }
                else
                {
                    unitNameListView2.setVisibility(View.GONE);
                    unitNameTextView2.setVisibility(View.GONE);
                }
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

        unitList1Adapter = new UnitList1Adapter();
        unitNames1 = graphSettings.getDataSet1Units();
        if (unitNames1.size() > 0)
        {
            for (int i = 0; i < unitNames1.size(); i++)
                unitList1Adapter.addItem(unitNames1.get(i));
        }

        ListView unitNameListView1 = (ListView) view.findViewById(R.id.Unitlist1);
        unitNameListView1.setAdapter(unitList1Adapter);

        unitNameListView2 = (ListView) view.findViewById(R.id.Unitlist2);
        TextView unitNameTextView2 = (TextView) view.findViewById(R.id.SelectedUnitText2);
        TextView unitNameTextView1 = (TextView) view.findViewById(R.id.SelectedUnitText1);

        String dataName1Text = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getSensorName() + " Units";
        unitNameTextView1.setText(dataName1Text);

        if (graphSettings.sensorHasUniqueDataSetUnits())
        {
            unitList2Adapter = new UnitList2Adapter();
            unitNames2 = graphSettings.getDataSet2Units();
            if (unitNames2.size() > 0)
            {
                for (int i = 0; i < unitNames2.size(); i++)
                    unitList2Adapter.addItem(unitNames2.get(i));
            }

            String dataName2Text = dataPointNames.get(1) + " Units";
            unitNameTextView2.setVisibility(View.VISIBLE);
            unitNameTextView2.setText(dataName2Text);
            unitNameListView2.setVisibility(View.VISIBLE);
            unitNameListView2.setAdapter(unitList2Adapter);
        }
        else {
            unitNameTextView2.setVisibility(View.GONE);
            unitNameListView2.setVisibility(View.GONE);
        }


        seekBarText = (TextView) view.findViewById(R.id.SeekBarTextView);
        dataSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        dataSeekBar.setProgress(graphSettingsFragInterface.getGraphConfig().getVisibleDataNum());
        seekBarText.setText(String.valueOf(String.valueOf(graphSettingsFragInterface.getGraphConfig().getVisibleDataNum())));
        dataSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        doneBtn = (ImageButton) view.findViewById(R.id.DoneBtn);
        doneBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Commit changes to visible data number in graph config
                graphSettingsFragInterface.getGraphConfig().setVisibleDataNum(dataSeekBar.getProgress());

                graphSettingsFragInterface.getGraphConfig().setSelectedUnitsPosition1(selectedUnitPosition1);
                graphSettingsFragInterface.getGraphConfig().setSelectedUnitsPosition2(selectedUnitPosition2);
                graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).setGraphUnits1(selectedUnitPosition1);
                graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).setGraphUnits2(selectedUnitPosition2);

                // commit changes to data points boolean list in graph config
                graphSettingsFragInterface.getGraphConfig().getDataPoints().set(0, dataPoints.get(0));
               // Log.i(TAG, "First:" + String.valueOf(dataPoints.get(0)));
                graphSettingsFragInterface.getGraphConfig().getDataPoints().set(1, dataPoints.get(1));
               // Log.i(TAG, "Second:" + String.valueOf(dataPoints.get(1)));
                graphSettingsFragInterface.getGraphConfig().getDataPoints().set(2, dataPoints.get(2));
              //  Log.i(TAG, "Third:" + String.valueOf(dataPoints.get(2)));

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                GraphFragment graphFragment = new GraphFragment();
                transaction.replace(R.id.fragment_container, graphFragment, GRAPH_FRAG_TAG);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        cancelBtn = (ImageButton) view.findViewById(R.id.CancelBtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                GraphFragment graphFragment = new GraphFragment();
                transaction.replace(R.id.fragment_container, graphFragment, GRAPH_FRAG_TAG);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // Initialize data points to what is in graphConfig
        dataPoints = new ArrayList<Boolean>();
        dataPoints.add(graphSettingsFragInterface.getGraphConfig().getDataPoints().get(0));
        dataPoints.add(graphSettingsFragInterface.getGraphConfig().getDataPoints().get(1));
        dataPoints.add(graphSettingsFragInterface.getGraphConfig().getDataPoints().get(2));

        selectedUnitPosition1 = graphSettingsFragInterface.getGraphConfig().getSelectedUnitsPosition1();
        selectedUnitPosition2 = graphSettingsFragInterface.getGraphConfig().getSelectedUnitsPosition2();

        return view;
    }

    private void reinitializeDataListView()
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

    private void reinitializeUnit1ListView()
    {
        unitList1Adapter.removeAllItems();
        GraphSettings graphSettings = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings();
        unitNames1 = graphSettings.getDataSet1Units();
        if (unitNames1.size() > 0)
        {
            for (int i = 0; i < unitNames1.size(); i++)
                unitList1Adapter.addItem(unitNames1.get(i));
        }
    }

    private void reinitializeUnit2ListView()
    {
        GraphSettings graphSettings = graphSettingsFragInterface.getSensor(graphSettingsFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings();
        if(unitList2Adapter == null) {
            unitList2Adapter = new UnitList2Adapter();
            unitNameListView2.setAdapter(unitList2Adapter);
        }
            unitList2Adapter.removeAllItems();
            unitNames2 = graphSettings.getDataSet2Units();
            if (unitNames2.size() > 0) {
                for (int i = 0; i < unitNames2.size(); i++)
                    unitList2Adapter.addItem(unitNames2.get(i));
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
        private String TAG = "DataListAdapter";
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

            if (dataPoints.get(position))
                convertView.setBackgroundColor(SensorConst.SELECTION_COLOR);
            else
                convertView.setBackgroundColor(Color.WHITE);

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (dataPoints.get(position)) {
                        dataPoints.set(position, false);
                        //arg0.setBackgroundColor(Color.WHITE);
                        notifyDataSetChanged();
                    }
                    else{
                        dataPoints.set(position, true);
                        //arg0.setBackgroundColor(Color.BLUE);
                        notifyDataSetChanged();
                    }
                }
            });

            return convertView;

        }
    }

    private class UnitList1Adapter extends BaseAdapter {
        private ArrayList<String> unitName = new ArrayList<String>();
        private LayoutInflater mInflater;
        private String TAG = "UnitListAdapter1";
        private int currentSelectedPosition;

        public UnitList1Adapter() {

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
                    if (position != selectedUnitPosition1) {
                        selectedUnitPosition1 = position;
                        notifyDataSetChanged();
                    }
                }
            });

            if (position == selectedUnitPosition1)
                convertView.setBackgroundColor(SensorConst.SELECTION_COLOR);
            else
                convertView.setBackgroundColor(Color.WHITE);

            return convertView;

        }
    }

    private class UnitList2Adapter extends BaseAdapter {
        private ArrayList<String> unitName = new ArrayList<String>();
        private LayoutInflater mInflater;
        private String TAG = "UnitListAdapter2";
        private int currentSelectedPosition;

        public UnitList2Adapter() {

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
                    if (position != selectedUnitPosition2) {
                        selectedUnitPosition2 = position;
                        notifyDataSetChanged();
                    }
                }
            });

            if (position == selectedUnitPosition2)
                convertView.setBackgroundColor(SensorConst.SELECTION_COLOR);
            else
                convertView.setBackgroundColor(Color.WHITE);

            return convertView;

        }
    }
}
