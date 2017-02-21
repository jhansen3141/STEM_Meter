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



    // Container Activity must implement this interface
    public interface GraphFragInterface {
        public ArrayList<LineData> getSavedList();
        public ArrayList<String> getSavedNameList();
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
        View view = inflater.inflate(R.layout.graph_fragment, container, false);
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
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
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
                }
                else
                {
                    playPauseBtn.setBackgroundResource(R.drawable.ic_play_arrow_black_24dp);
                    playPauseBtn.setChecked(false);
                    Log.i(TAG,"Pause Button Clicked");
                }
            }
        });

        //series = new DynamicSeries("Acceleration");

        //seriesFormatter = new LineAndPointFormatter();
        //seriesFormatter.setPointLabelFormatter(new PointLabelFormatter());
        //seriesFormatter.configure(getActivity().getApplicationContext(),
        //        R.xml.line_point_formatter_with_labels);
        //seriesFormatter.setPointLabelFormatter(new PointLabelFormatter(Color.TRANSPARENT));

        //plot.addSeries(series,seriesFormatter);

        //plot.setDomainBoundaries(0, 300, BoundaryMode.FIXED );
        //plot.setRangeBoundaries(-3,3,BoundaryMode.FIXED);

        return view;
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
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

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.BLUE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {

                    // Don't generate garbage runnables inside the loop.
                    getActivity().runOnUiThread(runnable);

                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();

        //plot.redraw();
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


    public void addPlot(float value) {

    }
}
