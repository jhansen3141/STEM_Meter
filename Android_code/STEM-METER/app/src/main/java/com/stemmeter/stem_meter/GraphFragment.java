package com.stemmeter.stem_meter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.mikephil.charting.charts.LineChart;
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

import java.util.ArrayList;
import java.util.Calendar;

import static com.stemmeter.stem_meter.R.id.chart;

/**
 * Created by Josh on 1/28/2017.
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
        SensorConfig getSensorConfig(int sensorNumber);
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
    private boolean noSensorConnected = true;
    private boolean selectedSensorIsOff = true;
    private boolean graphIsOff = true;

    private GraphFileStorage graphFileStorage;

    private View graphView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        graphView = inflater.inflate(R.layout.graph_fragment, container, false);

        graphFileStorage = new GraphFileStorage();

        //plot = (XYPlot) view.findViewById(plot);
        mChart = (LineChart) graphView.findViewById(chart);

        // disable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        selectedSensor = graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1);
        SensorConfig selectedSensorConfig = graphFragInterface.getSensorConfig(graphFragInterface.getGraphConfig().getSelectedSensor() + 1);
        if (selectedSensor != null) {
            noSensorConnected = false;
           // Log.i(TAG, "Selected Sensor is not null");
            if (selectedSensorConfig.getFreq() == SensorConst.RATE_OFF || selectedSensorConfig.getFreq() == SensorConst.RATE_INFO) {
                selectedSensorIsOff = true;
            }
            else {
                selectedSensorIsOff = false;
            }
        }

        graphIsOff = selectedSensorIsOff || noSensorConnected;

        if (noSensorConnected) {
            mChart.setNoDataText("No Sensor Selected");
        }
        else if (selectedSensorIsOff) {
            mChart.setNoDataText("Selected Sensor is turned off");
        }

        if (!graphIsOff) {
            LineData data = new LineData();
            data.setValueTextColor(Color.BLUE);

            // add empty data
            mChart.setData(data);

            // get the legend (only possible after setting data)
            Legend l = mChart.getLegend();

            // modify the legend ...
            l.setForm(Legend.LegendForm.LINE);
            l.setTypeface(Typeface.DEFAULT);
            l.setTextColor(Color.BLACK);

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
            leftAxis.setDrawGridLines(true);
            //leftAxis.setTitle(graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1).getGraphSettings().getUnits().get(graphFragInterface.getGraphConfig().getSelectedUnitsPosition()));

            YAxis rightAxis = mChart.getAxisRight();
            rightAxis.setEnabled(false);
        }

        zeroBtn = (ImageButton) graphView.findViewById(R.id.ZeroBtn);
        zeroBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                graphFragInterface.getSensor((graphFragInterface.getGraphConfig().getSelectedSensor()+1)).zeroSensor();
            }
        });

        //mChart.setVisibleXRangeMaximum(10);
        settingsBtn = (ImageButton) graphView.findViewById(R.id.GraphSettingsBtn);
        settingsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                GraphSettingsFragment graphSettingsFragment = new GraphSettingsFragment();
                transaction.replace(R.id.fragment_container, graphSettingsFragment, GRAPHSETTINGS_FRAG_TAG);
                transaction.commit();
            }
        });


        saveBtn = (ImageButton) graphView.findViewById(R.id.SaveBtn);
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
                        SavedGraphData savedGraphData = new SavedGraphData(input.getText().toString() + "-" +
                                Calendar.getInstance().getTime().toString(),
                                mChart.getData(), 1, graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1).
                                getGraphSettings().getDataSet1Units().get(graphFragInterface.getGraphConfig().getSelectedUnitsPosition1()));

                        graphFragInterface.getSavedGraphDataList().add(savedGraphData);
                        graphFileStorage.saveGraphFile(getActivity(),graphFragInterface.getSavedGraphDataList());
                        dialog.cancel();

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), input.getText().toString() +  " Graph Saved", Toast.LENGTH_SHORT).show();
                            }
                        });
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
            }
        });


        playPauseBtn = (ToggleButton) graphView.findViewById(R.id.PlayPauseBtn);
        playPauseBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (playPauseBtn.isChecked()) {
                    playPauseBtn.setBackgroundResource(R.drawable.ic_pause_circle_filled_black_24dp);
                    playPauseBtn.setChecked(true);
                    //  Log.i(TAG,"Play Button Clicked");
                    if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_PLAY) {
                        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_PLAY);
                        mChart.clearValues();
                        mChart.fitScreen();
                        saveBtn.setEnabled(false);
                        saveBtn.setColorFilter(Color.GRAY);
                        // Zero out the x axis
                        selectedSensor.zeroX();
                    }
                }
                else
                {
                    playPauseBtn.setBackgroundResource(R.drawable.black_record);
                    playPauseBtn.setChecked(false);
                    //     Log.i(TAG,"Pause Button Clicked");
                    if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_PAUSE) {
                        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_PAUSE);
                        saveBtn.setEnabled(true);
                        saveBtn.setColorFilter(Color.BLACK);
                    }
                }
            }
        });

        stopBtn = (ImageButton) graphView.findViewById(R.id.GraphStopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP)
                    return;

                playPauseBtn.setBackgroundResource(R.drawable.black_record);
                playPauseBtn.setChecked(false);
                graphFragInterface.getGraphConfig().setState(GRAPH_STATE_STOP);
                mChart.clearValues();
                mChart.fitScreen();
                selectedSensor.zeroX();
            }
        });

        if (!graphIsOff)
        {
            ArrayList<Boolean> visibleDataSets = graphFragInterface.getGraphConfig().getDataPoints();
            Sensor sensor = graphFragInterface.getSensor(graphFragInterface.getGraphConfig().getSelectedSensor() + 1);

            ArrayList<String> dataSetNames = sensor.getGraphSettings().getDataPoints();

            String dataSet1Units = selectedSensor.getGraphSettings().getDataSet1Units().get(graphFragInterface.getGraphConfig().getSelectedUnitsPosition1());
            String dataSet2Units = null;

            if (selectedSensor.getGraphSettings().sensorHasUniqueDataSetUnits())
                dataSet2Units = selectedSensor.getGraphSettings().getDataSet2Units().get(graphFragInterface.getGraphConfig().getSelectedUnitsPosition2());

            if (visibleDataSets.get(0) && dataSetNames.size() > 0) {
                dataSetName1 = dataSetNames.get(0) + "-" + dataSet1Units;
            }
            else if (visibleDataSets.get(1) && dataSetNames.size() > 1) {
                if (selectedSensor.getGraphSettings().sensorHasUniqueDataSetUnits()) {
                    dataSetName1 = dataSetNames.get(1) + "-" + dataSet2Units;
                }
                else {
                    dataSetName1 = dataSetNames.get(1) + "-" + dataSet1Units;
                }
            }
            else if (dataSetNames.size() > 2)
                dataSetName1 = dataSetNames.get(2) + "-" + dataSet1Units;

            if (visibleDataSets.get(1) && dataSetNames.size() > 1)
                if (selectedSensor.getGraphSettings().sensorHasUniqueDataSetUnits()) {
                    dataSetName2 = dataSetNames.get(1) + "-" + dataSet2Units;
                }
                else
                {
                    dataSetName2 = dataSetNames.get(1) + "-" + dataSet1Units;
                }
            else if (dataSetNames.size() > 2) {
                dataSetName2 = dataSetNames.get(2) + "-" + dataSet1Units;
            }

            if (dataSetNames.size() > 2) {
                dataSetName3 = dataSetNames.get(2) + "-" + dataSet1Units;
            }

        }
        else {
            saveBtn.setVisibility(View.INVISIBLE);
//            settingsBtn.setVisibility(View.INVISIBLE);
            zeroBtn.setVisibility(View.INVISIBLE);
            stopBtn.setVisibility(View.INVISIBLE);
            playPauseBtn.setVisibility(View.INVISIBLE);
        }

        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_STOP);
        return graphView;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Zero out the x axis every time graph screen is shown
        if (!graphIsOff) {
            selectedSensor.zeroX();
        }
    }

    private void addEntry(float yValue, float xValue) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

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
            if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_STOP)
                mChart.setVisibleXRangeMaximum((graphFragInterface.getGraphConfig().getVisibleDataNum() - 1) * selectedSensor.getRateMult());

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
                set2 = createSet(Color.rgb(140,0,0), Color.RED, dataSetName2);
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
            if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_STOP)
                mChart.setVisibleXRangeMaximum((graphFragInterface.getGraphConfig().getVisibleDataNum() - 1) * selectedSensor.getRateMult());
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
                mChart.moveViewToX(xValue);
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
                set2 = createSet(Color.rgb(140,0,0), Color.RED, dataSetName2);
                data.addDataSet(set2);
            }

            if (set3 == null)
            {
                set3 = createSet(Color.rgb(0,140,0), Color.GREEN, dataSetName3);
                data.addDataSet(set3);
            }

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP && (xValue > ((graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult())))
                xValue = (graphFragInterface.getGraphConfig().getVisibleDataNum() + 1) * selectedSensor.getRateMult();

            data.addEntry(new Entry(xValue, yValue1), 0);
            data.addEntry(new Entry(xValue, yValue2), 1);
            data.addEntry(new Entry(xValue, yValue3), 2);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                mChart.getXAxis().setEnabled(false);
                Entry entry;
                //removing last element from the chart and finding max and min visible value
                if ((set1.getEntryCount() - 1) > graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        float before = entry.getX();
                        entry.setX(entry.getX() - selectedSensor.getRateMult());
                        float after = entry.getX();
                       // Log.i(TAG, "Before: " + String.valueOf(before) + " After: " + String.valueOf(after));
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
        set.setValueTextColor(circleColor);
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

    public void addGraphEntry(final SensorReading sensorReading, final int numberDataPoints) {

        if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_PAUSE ) {
            return;
        }
        // Have to run all graph entry ops on UI thread. MPAndroid chart not thread safe
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }
}
