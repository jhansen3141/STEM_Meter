package com.stemmeter.stem_meter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import static com.stemmeter.stem_meter.R.id.ListBtn;
import static com.stemmeter.stem_meter.R.id.chart;

/**
 * Created by monro on 2/19/2017.
 */

public class DisplayFragment extends Fragment {

    DisplayFragInterface displayFragInterface;

    // Container Activity must implement this interface
    public interface DisplayFragInterface {
//        public ArrayList<LineData> getSavedList();
//        public ArrayList<String> getSavedNameList();
        public ArrayList<SavedGraphData> getSavedGraphDataList();
        public void setSavedGraphDataList(ArrayList<SavedGraphData> savedGraphData);
    }

    private LineChart mChart;
    private String TAG = "DisplayFragTag";
    //@Override
    //public void onActivityCreated(Bundle savedInstanceState) {
    //    super.onActivityCreated(savedInstanceState);
    //}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.display_fragment, container, false);

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

        //LineData data = new LineData();
        //data.setValueTextColor(Color.BLUE);

        // add empty data
        //mChart.setData(data);

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

        DataListAdapter dataListAdapter = new DataListAdapter();
        List<SavedGraphData> savedGraphData = displayFragInterface.getSavedGraphDataList();
        List<String> dataNameList = new ArrayList<String>();

        for (int i = 0; i < savedGraphData.size(); i++)
            dataNameList.add(savedGraphData.get(i).getName());

        //List<String> dataNameList = displayFragInterface.getSavedNameList();
        if (dataNameList.size() > 0)
        {
            for (int i = 0; i < dataNameList.size(); i++)
                dataListAdapter.addItem(dataNameList.get(i));
        }

        ListView graphListView = (ListView) view.findViewById(R.id.graphlist);
        graphListView.setAdapter(dataListAdapter);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            displayFragInterface = (DisplayFragment.DisplayFragInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DisplayFragInterface");
        }
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        if (displayFragInterface.getSavedGraphDataList() != null) {
//            SharedPreferences pref = this.getActivity().getSharedPreferences("info", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = pref.edit();
//            Gson gson = new Gson();
//            String json = gson.toJson(displayFragInterface.getSavedGraphDataList());
//            editor.putString(TAG, json);
//            editor.commit();
//        }
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        SharedPreferences pref = this.getActivity().getSharedPreferences("info", Context.MODE_PRIVATE);
//        String json = pref.getString(TAG,"");
//        if (!json.isEmpty())
//        {
//            Log.i(TAG, "Getting Saved Data");
//            Gson gson = new Gson();
//            ArrayList<SavedGraphData> savedGraphDataList = (ArrayList<SavedGraphData>) gson.fromJson(json, new TypeToken<ArrayList<SavedGraphData>>(){}.getType());
//            if (savedGraphDataList != null && savedGraphDataList.size() > 0)
//                displayFragInterface.setSavedGraphDataList(savedGraphDataList);
//        }
//
//
//    }

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

    private void addEntry(LineData data) {

        //LineData data = mChart.getData();

        //if (data != null) {

            //ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            //if (set == null) {
            //    set = createSet();
            //    data.addDataSet(set);
            //}

            mChart.setData(data);
            data.notifyDataChanged();
            //data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
            //data.notifyDataChanged();

            mChart.getAxisLeft().resetAxisMinimum();
            mChart.getAxisLeft().resetAxisMaximum();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            mChart.animateXY(1000,1000);
            //mChart.invalidate();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(10);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            //mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        //}
    }

    private class DataListAdapter extends BaseAdapter {

        private ArrayList<String> graphName = new ArrayList<String>();
        private LayoutInflater mInflater;
        //private final ArrayList<SensorsFragment.SensorListAdapter.SetBoolean> setBooleanList = new ArrayList<SensorsFragment.SensorListAdapter.SetBoolean>();
        private String TAG = "CustomAdapter";
        private int currentSelectedPosition;
        private int selectedPosition = -1;

        public DataListAdapter() {

            mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final String item) {
            graphName.add(item);
            notifyDataSetChanged();
        }

        public void updateItem(final String item, int position) {
            graphName.set(position,item);

        }

        @Override
        public int getCount() {
        return graphName.size();
        }

        @Override
        public String getItem(int position) {
            return graphName.get(position);
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
                convertView = mInflater.inflate(R.layout.data_list_item, null);
            }

            dataNameText = (TextView) convertView.findViewById(R.id.dataNameTextView);
            deleteBtn = (ImageButton) convertView.findViewById(R.id.DeleteBtn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    graphName.remove(position);

                    displayFragInterface.getSavedGraphDataList().remove(position);
//                    displayFragInterface.getSavedNameList().remove(position);
//                    displayFragInterface.getSavedList().remove(position);

                    if (selectedPosition == position) {
                        mChart.clear();
                        selectedPosition = -1;
                    }

                    if (position < selectedPosition)
                        --selectedPosition;

                    notifyDataSetChanged();
                }
            });

            if (position == selectedPosition)
                convertView.setBackgroundColor(Color.BLUE);
            else
                convertView.setBackgroundColor(Color.WHITE);

            if (dataNameText != null)
                dataNameText.setText(graphName.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    addEntry(displayFragInterface.getSavedGraphDataList().get(position).getData());
                    Log.i(TAG, "Graph List Item clicked");
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });

            return convertView;

        }
    }
}
