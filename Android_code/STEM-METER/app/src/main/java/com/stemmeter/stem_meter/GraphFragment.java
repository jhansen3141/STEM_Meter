package com.stemmeter.stem_meter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.stemmeter.stem_meter.Sensors.Sensor;
import com.stemmeter.stem_meter.Sensors.SensorReading;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.stemmeter.stem_meter.R.id.chart;
//import static com.example.josh.boardtestx.R.id.plot;

/**
 * Created by Josh on 11/28/2016.
 */

public class GraphFragment extends Fragment {

    public static final int GRAPH_STATE_STOP = 0;
    public static final int GRAPH_STATE_PLAY = 1;
    public static final int GRAPH_STATE_PAUSE = 2;

    private final String GRAPHSETTINGS_FRAG_TAG = "GraphSettingsFragTag";

    // Container Activity must implement this interface
    public interface GraphFragInterface {
        ArrayList<SavedGraphData> getSavedGraphDataList();
        GraphConfig getGraphConfig();
        Sensor getSensor(int sensorNumber);
    }

    GraphFragInterface graphFragInterface;
    private String TAG = "GraphFragment";
    //private XYPlot plot;
    private LineChart mChart;
    private ToggleButton playPauseBtn;
    private ImageButton stopBtn;
    private ImageButton saveBtn;
    private ImageButton settingsBtn;
    private ImageButton zeroBtn;
    private String dataSetName1;
    private String dataSetName2;
    private String dataSetName3;
    private Sensor selectedSensor;

    private GraphFileStorage graphFileStorage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.graph_fragment, container, false);

        graphFileStorage = new GraphFileStorage();

        //plot = (XYPlot) view.findViewById(plot);
        mChart = (LineChart) view.findViewById(chart);
        mChart.setNoDataText("No data for the moment");

        //mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.BLUE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

      //  mChart.getLegend().setTextSize(11f);

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(Typeface.DEFAULT);
        l.setTextColor(Color.BLACK);
        //l.setTextSize(12f);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(Typeface.DEFAULT);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTitle("Seconds");

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(Color.BLACK);
        //leftAxis.setAxisMaximum(0);
        //leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(true);
        leftAxis.setTitle(graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings().getUnits().get(graphFragInterface.getGraphConfig().getSelectedUnitsPosition()));

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        zeroBtn = (ImageButton) view.findViewById(R.id.ZeroBtn);
        zeroBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                graphFragInterface.getSensor((graphFragInterface.getGraphConfig().getSelectedSensor()+1)).zeroSensor();
            }
        });

        //mChart.setVisibleXRangeMaximum(10);
        settingsBtn = (ImageButton) view.findViewById(R.id.GraphSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                GraphSettingsFragment graphSettingsFragment = new GraphSettingsFragment();
                transaction.replace(R.id.fragment_container, graphSettingsFragment, GRAPHSETTINGS_FRAG_TAG);
                //transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        saveBtn = (ImageButton) view.findViewById(R.id.SaveBtn);
        saveBtn.setEnabled(false);
        saveBtn.setColorFilter(Color.GRAY);
        saveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());

                // set title
                alertDialogBuilder.setTitle("Graph Name");

                final EditText input = new EditText(getActivity());
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialogBuilder.setView(input);

                // set dialog message
                alertDialogBuilder
                        .setMessage("Enter Graph Name")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                SavedGraphData savedGraphData = new SavedGraphData(input.getText().toString(), mChart.getData(), 1, graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings().getUnits().get(graphFragInterface.getGraphConfig().getSelectedUnitsPosition()));

                                graphFragInterface.getSavedGraphDataList().add(savedGraphData);
                                graphFileStorage.saveGraphFile(getActivity(),graphFragInterface.getSavedGraphDataList());
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                //LineData data = mChart.getData();
            }

        });


        playPauseBtn = (ToggleButton) view.findViewById(R.id.PlayPauseBtn);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (playPauseBtn.isChecked()) {
                    playPauseBtn.setBackgroundResource(R.drawable.ic_pause_black_24dp);
                    playPauseBtn.setChecked(true);
                    Log.i(TAG,"Play Button Clicked");
                    if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_PLAY) {
                        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_PLAY);
                        mChart.clearValues();
                        mChart.fitScreen();
                        saveBtn.setEnabled(false);
                        saveBtn.setColorFilter(Color.GRAY);
                        // Zero out the x axis
                        graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor()+1).zeroX();
                    }
                }
                else
                {
                    playPauseBtn.setBackgroundResource(R.drawable.ic_fiber_manual_record_black_24dp);
                    playPauseBtn.setChecked(false);
                    Log.i(TAG,"Pause Button Clicked");
                    if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_PAUSE) {
                        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_PAUSE);
                        saveBtn.setEnabled(true);
                        saveBtn.setColorFilter(Color.BLACK);
                    }
                }
            }
        });

        stopBtn = (ImageButton) view.findViewById(R.id.GraphStopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP)
                    return;

                playPauseBtn.setBackgroundResource(R.drawable.ic_fiber_manual_record_black_24dp);
                playPauseBtn.setChecked(false);
                graphFragInterface.getGraphConfig().setState(GRAPH_STATE_STOP);
                mChart.clearValues();
                mChart.fitScreen();
            }
        });

        ArrayList<Boolean> visibleDataSets = graphFragInterface.getGraphConfig().getDataPoints();
        Sensor sensor = graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1);

        if (sensor != null) {
            ArrayList<String> dataSetNames = sensor.getGraphSettings().getDataPoints();

            if (visibleDataSets.get(0) && dataSetNames.size() > 0)
                dataSetName1 = dataSetNames.get(0);
            else if (visibleDataSets.get(1) && dataSetNames.size() > 1)
                dataSetName1 = dataSetNames.get(1);
            else if (dataSetNames.size() > 2)
                dataSetName1 = dataSetNames.get(2);

            if (visibleDataSets.get(1) && dataSetNames.size() > 1)
                dataSetName2 = dataSetNames.get(1);
            else if (dataSetNames.size() > 2)
                dataSetName2 = dataSetNames.get(2);

            if (dataSetNames.size() > 2)
            dataSetName3 = dataSetNames.get(2);
        }

        selectedSensor = graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1);
        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_STOP);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Zero out the x axis every time graph screen is shown
        selectedSensor.zeroX();
    }

    private void addEntry(float yValue, float xValue) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(Color.BLUE, ColorTemplate.getHoloBlue(), dataSetName1);
                data.addDataSet(set);
            }

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP && (xValue > ((graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult())))
                xValue = (graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult();

            data.addEntry(new Entry(xValue, yValue), 0);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {

                mChart.getXAxis().setEnabled(false);

                Entry entry;

                if ((set.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set.removeEntry(0);

                    for (int i = 0; i < set.getEntryCount(); i++) {
                        entry = set.getEntryForIndex(i);
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                    }
                }
            }
            else {
                mChart.getXAxis().setEnabled(true);
            }

            if (yValue > (mChart.getYMax())) {
                mChart.getAxisLeft().setAxisMaximum(yValue + (Math.abs(yValue * 0.5f)));
            }

            if (yValue < (mChart.getYMin())) {
                mChart.getAxisLeft().setAxisMinimum(yValue - (Math.abs(yValue * 0.5f)));
            }

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            if (xValue == selectedSensor.getRateMult())
                mChart.fitScreen();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum((graphFragInterface.getGraphConfig().getVisibleDataNum() - 1) * selectedSensor.getRateMult());
            //Log.i(TAG, String.valueOf(graphFragInterface.getGraphConfig().getVisibleDataNum()));
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(xValue);

        }
    }

    private void addEntry(float yValue1, float yValue2, float xValue) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            // set.addEntry(...); // can be called as well

            if (set1 == null) {
                set1 = createSet(Color.BLUE, ColorTemplate.getHoloBlue(), dataSetName1);
                data.addDataSet(set1);
            }

            if (set2 == null)
            {
                set2 = createSet(Color.RED, Color.RED, dataSetName2);
                data.addDataSet(set2);
            }

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP && (xValue > ((graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult())))
                xValue = (graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult();

            data.addEntry(new Entry(xValue, yValue1), 0);
            data.addEntry(new Entry(xValue, yValue2), 1);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {

                mChart.getXAxis().setEnabled(false);

                Entry entry;

                if ((set1.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                    }
                }

                if ((set2.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set2.removeEntry(0);

                    for (int i = 0; i < set2.getEntryCount(); i++) {
                        entry = set2.getEntryForIndex(i);
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                    }
                }
            }
            else {
                mChart.getXAxis().setEnabled(true);
            }

            float maxValue = Math.max(yValue1,yValue2);
            float minValue = Math.min(yValue1, yValue2);

            if (maxValue > (mChart.getYMax())) {
                mChart.getAxisLeft().setAxisMaximum(maxValue + (Math.abs(maxValue*0.5f)));
            }

            if (minValue < (mChart.getYMin())) {
                mChart.getAxisLeft().setAxisMinimum(minValue - (Math.abs(minValue*0.5f)));
            }

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            if (xValue == selectedSensor.getRateMult())
                mChart.fitScreen();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum((graphFragInterface.getGraphConfig().getVisibleDataNum() - 1) * selectedSensor.getRateMult());
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(xValue);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private void addEntry(float yValue1, float yValue2, float yValue3, float xValue) {
        //mChart.setVisibleXRangeMaximum(10);
        //boolean firstTime = false;
        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
            // set.addEntry(...); // can be called as well

            if (set1 == null) {
                set1 = createSet(Color.BLUE, ColorTemplate.getHoloBlue(), dataSetName1);
                data.addDataSet(set1);
                //firstTime = true;
            }

            if (set2 == null)
            {
                set2 = createSet(Color.RED, Color.RED, dataSetName2);
                data.addDataSet(set2);
            }

            if (set3 == null)
            {
                set3 = createSet(Color.GREEN, Color.DKGRAY, dataSetName3);
                data.addDataSet(set3);
            }

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP && (xValue > ((graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult())))
                xValue = (graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult();

            data.addEntry(new Entry(xValue, yValue1), 0);
            data.addEntry(new Entry(xValue, yValue2), 1);
            data.addEntry(new Entry(xValue, yValue3), 2);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                mChart.getXAxis().setEnabled(true);
                Entry entry;
                //removing last element from the chart and finding max and min visible value
                if ((set1.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        float before = entry.getX();
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                        float after = entry.getX();
                        Log.i(TAG, "Before: " + String.valueOf(before) + " After: " + String.valueOf(after));
                    }
                }

                if ((set2.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set2.removeEntry(0);

                    for (int i = 0; i < set2.getEntryCount(); i++) {
                        entry = set2.getEntryForIndex(i);
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                    }
                }

                if ((set3.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set3.removeEntry(0);

                    for (int i = 0; i < set3.getEntryCount(); i++) {
                        entry = set3.getEntryForIndex(i);
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                    }
                }
            }
            else {
                mChart.getXAxis().setEnabled(true);
            }

            data.notifyDataChanged();

            float maxValue = Math.max(yValue1,yValue2);
            if (yValue3 > maxValue) {
                maxValue = yValue3;
            }

            float minValue = Math.min(yValue1, yValue2);
            if (yValue3 < minValue) {
                minValue = yValue3;
            }

            if (maxValue > (mChart.getYMax())) {
                mChart.getAxisLeft().setAxisMaximum(maxValue + (Math.abs(maxValue*0.5f)));
            }

            if (minValue < (mChart.getYMin())) {
                mChart.getAxisLeft().setAxisMinimum(minValue - (Math.abs(minValue*0.5f)));
            }

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            //mChart.invalidate();

            if (xValue == selectedSensor.getRateMult())
                mChart.fitScreen();

            // limit the number of visible entries
            if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_STOP)
                mChart.setVisibleXRangeMaximum((graphFragInterface.getGraphConfig().getVisibleDataNum() - 1) * selectedSensor.getRateMult());

            // move to the latest entry
            mChart.moveViewToX(xValue);
        }
    }

    private LineDataSet createSet(int circleColor, int color, String name) {

        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setCircleColor(circleColor);
        set.setLineWidth(2f);
        set.setCircleRadius(2f);
        set.setFillAlpha(65);
        set.setFillColor(color);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(color);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            graphFragInterface = (GraphFragment.GraphFragInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement GraphFraInterface");
        }
    }


    public void addGraphEntry(SensorReading sensorReading, int numberDataPoints) {

        if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_PAUSE )
            return;

        switch(numberDataPoints)
        {
            case 1:
                if (graphFragInterface.getGraphConfig().getDataPoints().get(0))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getSensorReadingTime());
                break;
            case 2:
                if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getGraphData().get(1), sensorReading.getSensorReadingTime());
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getSensorReadingTime());
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(1))
                    addEntry(sensorReading.getGraphData().get(1), sensorReading.getSensorReadingTime());
                break;
            case 3:
                if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getGraphData().get(1), sensorReading.getGraphData().get(2), sensorReading.getSensorReadingTime());
                else if (!graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(1), sensorReading.getGraphData().get(2), sensorReading.getSensorReadingTime());
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && !graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getGraphData().get(2), sensorReading.getSensorReadingTime());
                else if (!graphFragInterface.getGraphConfig().getDataPoints().get(0) && !graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(2), sensorReading.getSensorReadingTime());
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && !graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getGraphData().get(1), sensorReading.getSensorReadingTime());
                else if (!graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && !graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(1), sensorReading.getSensorReadingTime());
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && !graphFragInterface.getGraphConfig().getDataPoints().get(1) && !graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorReading.getGraphData().get(0), sensorReading.getSensorReadingTime());
                break;
        }
    }
}
