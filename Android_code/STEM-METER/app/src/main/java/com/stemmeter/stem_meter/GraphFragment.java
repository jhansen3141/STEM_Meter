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

    // Container Activity must implement this interface
    public interface GraphFragInterface {
        public ArrayList<LineData> getSavedList();
        public ArrayList<String> getSavedNameList();
        public GraphConfig getGraphConfig();
    }

    GraphFragInterface graphFragInterface;
    private String TAG = "GraphFragment";
    //private XYPlot plot;
    private LineChart mChart;
    private ToggleButton playPauseBtn;
    private ImageButton saveBtn;
    private long currentIndex = 0;

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
        leftAxis.setAxisMaximum(0.2f);
        leftAxis.setAxisMinimum(-0.2f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        //mChart.setVisibleXRangeMaximum(10);

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
                set = createSet(Color.BLUE, ColorTemplate.getHoloBlue());
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                Entry entry;

                if (set.getEntryCount() == 11) {
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
            mChart.setVisibleXRangeMaximum(10);
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
                set1 = createSet(Color.BLUE, ColorTemplate.getHoloBlue());
                data.addDataSet(set1);
            }

            if (set2 == null)
            {
                set2 = createSet(Color.RED, Color.RED);
                data.addDataSet(set2);
            }

            data.addEntry(new Entry(set1.getEntryCount(), num1), 0);
            data.addEntry(new Entry(set2.getEntryCount(), num2), 1);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                Entry entry;

                if (set1.getEntryCount() == 11) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }

                if (set2.getEntryCount() == 11) {
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
            mChart.setVisibleXRangeMaximum(10);
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
                set1 = createSet(Color.BLUE, ColorTemplate.getHoloBlue());
                data.addDataSet(set1);
            }

            if (set2 == null)
            {
                set2 = createSet(Color.RED, Color.RED);
                data.addDataSet(set2);
            }

            if (set3 == null)
            {
                set3 = createSet(Color.GREEN, Color.DKGRAY);
                data.addDataSet(set3);
            }

            data.addEntry(new Entry(set1.getEntryCount(), num1), 0);
            data.addEntry(new Entry(set2.getEntryCount(), num2), 1);
            data.addEntry(new Entry(set3.getEntryCount(), num3), 2);

            if (graphFragInterface.getGraphConfig().getState() == GRAPH_STATE_STOP) {
                Entry entry;
//removing last element from the chart and finding max and min visible value
                if (set1.getEntryCount() == 11) {
                    set1.removeEntry(0);

                    for (int i = 0; i < set1.getEntryCount(); i++) {
                        entry = set1.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }

                if (set2.getEntryCount() == 11) {
                    data.removeEntry(0, 1);

                    for (int i = 0; i < set2.getEntryCount(); i++) {
                        entry = set2.getEntryForIndex(i);
                        entry.setX(entry.getX() - 1);
                    }
                }

                if (set3.getEntryCount() == 11) {
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
            mChart.setVisibleXRangeMaximum(10);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
            Log.i(TAG,"Set1, Set2, Set3" + set1.getEntryCount() + set2.getEntryCount() + set3.getEntryCount());
        }
    }

    private LineDataSet createSet(int circleColor, int color) {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
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
                addEntry(sensorDataList.get(0));
                break;
            case 2:
                addEntry(sensorDataList.get(0), sensorDataList.get(1));
                break;
            case 3:
                addEntry(sensorDataList.get(0), sensorDataList.get(1), sensorDataList.get(2));
                break;
        }
    }
}
