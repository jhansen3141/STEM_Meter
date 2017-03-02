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
        public ArrayList<LineData> getSavedList();
        public ArrayList<String> getSavedNameList();
        public GraphConfig getGraphConfig();
        public Sensor getSensor(int sensorNumber);
    }

    GraphFragInterface graphFragInterface;
    private String TAG = "GraphFragment";
    //private XYPlot plot;
    private LineChart mChart;
    private ToggleButton playPauseBtn;
    private ImageButton saveBtn;
    private ImageButton settingsBtn;
    private long currentIndex = 0;
    private String dataSetName1;
    private String dataSetName2;
    private String dataSetName3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.graph_fragment, container, false);
        //plot = (XYPlot) view.findViewById(plot);
        mChart = (LineChart) view.findViewById(chart);
        mChart.setNoDataText("No data for the moment");

        //mChart.setOnChartValueSelectedListener(this);

        // enable description text
        //mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

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

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaximum(-9999999.2f);
        leftAxis.setAxisMinimum(9999999.2f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

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

                                graphFragInterface.getSavedList().add(mChart.getData());
                                graphFragInterface.getSavedNameList().add(input.getText().toString());
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
                LineData data = mChart.getData();
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
                        saveBtn.setVisibility(View.GONE);
                    }
                }
                else
                {
                    playPauseBtn.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                    playPauseBtn.setChecked(false);
                    Log.i(TAG,"Pause Button Clicked");
                    if (graphFragInterface.getGraphConfig().getState() != GRAPH_STATE_PAUSE) {
                        graphFragInterface.getGraphConfig().setState(GRAPH_STATE_PAUSE);
                        saveBtn.setVisibility(View.VISIBLE);
                    }
                }
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

        return view;
    }

    private void addEntry(float num1) {

        if (num1 > (mChart.getYChartMax() - 0.5)) {
            mChart.getAxisLeft().setAxisMaximum(num1 + (float)0.5);
            //mChart.getAxisLeft().resetAxisMaximum();
        }

        if (num1 < (mChart.getYChartMin() + 0.5)) {
            mChart.getAxisLeft().setAxisMinimum(num1 - (float)0.5);
            //mChart.getAxisLeft().resetAxisMinimum();
        }

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(Color.BLUE, ColorTemplate.getHoloBlue(), dataSetName1);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), num1), 0);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                Entry entry;

                if (set.getEntryCount() == graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set.removeEntry(0);

                    for (int i = 0; i < set.getEntryCount(); i++) {
                        entry = set.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }
            }

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(graphFragInterface.getGraphConfig().getVisibleDataNum());
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private void addEntry(float num1, float num2) {

        float maxValue = -9999999;
        float minValue =  9999999;

        maxValue = Math.max(num1,num2);
        minValue = Math.min(num1, num2);

        if (maxValue > (mChart.getYChartMax() - 0.5)) {
            mChart.getAxisLeft().setAxisMaximum(maxValue + (float)0.5);
            //mChart.getAxisLeft().resetAxisMaximum();
        }

        if (minValue < (mChart.getYChartMin() + 0.5)) {
            mChart.getAxisLeft().setAxisMinimum(minValue - (float)0.5);
            //mChart.getAxisLeft().resetAxisMinimum();
        }

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

            data.addEntry(new Entry(set1.getEntryCount(), num1), 0);
            data.addEntry(new Entry(set2.getEntryCount(), num2), 1);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                Entry entry;

                if (set1.getEntryCount() == graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }

                if (set2.getEntryCount() == graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    data.removeEntry(0, 1);

                    for (int i = 0; i < set2.getEntryCount(); i++) {
                        entry = set2.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }
            }

            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(graphFragInterface.getGraphConfig().getVisibleDataNum());
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private void addEntry(float num1, float num2, float num3) {
        //mChart.setVisibleXRangeMaximum(10);

        float maxValue = -9999999;
        float minValue =  9999999;

        maxValue = Math.max(num1,num2);
        if (num3 > maxValue)
            maxValue = num3;

        minValue = Math.min(num1, num2);
        if (num3 < minValue)
            minValue = num3;

        if (maxValue > (mChart.getYChartMax() - 0.5)) {
            mChart.getAxisLeft().setAxisMaximum(maxValue + (float)0.5);
            //mChart.getAxisLeft().resetAxisMaximum();
        }

        if (minValue < (mChart.getYChartMin() + 0.5)) {
            mChart.getAxisLeft().setAxisMinimum(minValue - (float)0.5);
            //mChart.getAxisLeft().resetAxisMinimum();
        }

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set1 = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(1);
            ILineDataSet set3 = data.getDataSetByIndex(2);
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

            if (set3 == null)
            {
                set3 = createSet(Color.GREEN, Color.DKGRAY, dataSetName3);
                data.addDataSet(set3);
            }

            data.addEntry(new Entry(set1.getEntryCount(), num1), 0);
            data.addEntry(new Entry(set2.getEntryCount(), num2), 1);
            data.addEntry(new Entry(set3.getEntryCount(), num3), 2);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                Entry entry;
//removing last element from the chart and finding max and min visible value
                if (set1.getEntryCount() == graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }

                if (set2.getEntryCount() == graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    data.removeEntry(0, 1);

                    for (int i = 0; i < set2.getEntryCount(); i++) {
                        entry = set2.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }

                if (set3.getEntryCount() == graphFragInterface.getGraphConfig().getVisibleDataNum()) {
                    data.removeEntry(0, 2);

                    for (int i = 0; i < set3.getEntryCount(); i++) {
                        entry = set3.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }
            }
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            //mChart.invalidate();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(graphFragInterface.getGraphConfig().getVisibleDataNum());
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
            Log.i(TAG,"Set1, Set2, Set3" + set1.getEntryCount() + set2.getEntryCount() + set3.getEntryCount());
        }
    }

    private LineDataSet createSet(int circleColor, int color, String name) {

        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setCircleColor(circleColor);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(color);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(color);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        final Runnable runnable = new Runnable() {
//
//            @Override
//            public void run() {
//                addEntry();
//            }
//        };
//
//        Thread thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                for (int i = 0; i < 100; i++) {
//
//                    // Don't generate garbage runnables inside the loop.
//                    getActivity().runOnUiThread(runnable);
//
//                    try {
//                        Thread.sleep(25);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
//
//        //plot.redraw();
//    }

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


    public void addGraphEntry(ArrayList<Float> sensorDataList, int numberDataPoints) {

        if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_PAUSE )
            return;

        switch(numberDataPoints)
        {
            case 1:
                if (graphFragInterface.getGraphConfig().getDataPoints().get(0))
                    addEntry(sensorDataList.get(0));
                break;
            case 2:
                if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1))
                    addEntry(sensorDataList.get(0), sensorDataList.get(1));
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0))
                    addEntry(sensorDataList.get(0));
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(1))
                    addEntry(sensorDataList.get(1));
                break;
            case 3:
                if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(0), sensorDataList.get(1), sensorDataList.get(2));
                else if (!graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(1), sensorDataList.get(2));
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && !graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(0), sensorDataList.get(2));
                else if (!graphFragInterface.getGraphConfig().getDataPoints().get(0) && !graphFragInterface.getGraphConfig().getDataPoints().get(1) && graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(2));
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && !graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(0), sensorDataList.get(1));
                else if (!graphFragInterface.getGraphConfig().getDataPoints().get(0) && graphFragInterface.getGraphConfig().getDataPoints().get(1) && !graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(1));
                else if (graphFragInterface.getGraphConfig().getDataPoints().get(0) && !graphFragInterface.getGraphConfig().getDataPoints().get(1) && !graphFragInterface.getGraphConfig().getDataPoints().get(2))
                    addEntry(sensorDataList.get(0));
                break;
        }
    }
}
