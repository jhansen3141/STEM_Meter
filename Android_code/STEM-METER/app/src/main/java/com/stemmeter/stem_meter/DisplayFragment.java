package com.stemmeter.stem_meter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.stemmeter.stem_meter.R.id.ListBtn;
import static com.stemmeter.stem_meter.R.id.chart;
import static com.stemmeter.stem_meter.R.id.visible;

/**
 * Created by monro on 2/19/2017.
 */

public class DisplayFragment extends Fragment {

    DisplayFragInterface displayFragInterface;

    // Container Activity must implement this interface
    public interface DisplayFragInterface {
        ArrayList<SavedGraphData> getSavedGraphDataList();
        void setSavedGraphDataList(ArrayList<SavedGraphData> savedGraphData);
    }

    private LineChart mChart;
    private GraphFileStorage graphFileStorage;
    private String TAG = "DisplayFragTag";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.display_fragment, container, false);
        graphFileStorage = new GraphFileStorage();
        mChart = (LineChart) view.findViewById(chart);
        mChart.setNoDataText("Select a graph from list");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // disable description text
        mChart.getDescription().setEnabled(false);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

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
        xl.setTitle("Seconds");

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(Color.BLACK);
       // leftAxis.setAxisMaximum(100f);
       // leftAxis.setAxisMinimum(0f);
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

    private void addEntry(LineData data, int position) {

            mChart.setData(data);
            data.notifyDataChanged();

            mChart.getAxisLeft().resetAxisMinimum();
            mChart.getAxisLeft().resetAxisMaximum();
            mChart.getAxisLeft().setTitle(displayFragInterface.getSavedGraphDataList().get(position).getUnits());

            mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            mChart.animateXY(1000,1000);

    }

    private class DataListAdapter extends BaseAdapter {

        private ArrayList<String> graphName = new ArrayList<String>();
        private LayoutInflater mInflater;
        //private final ArrayList<SensorsFragment.SensorListAdapter.SetBoolean> setBooleanList = new ArrayList<SensorsFragment.SensorListAdapter.SetBoolean>();
        private String TAG = "CustomAdapter";
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
            final ImageButton cameraBtn;

            if (convertView == null) {
                // if the view is null then inflate the custom item layout
                convertView = mInflater.inflate(R.layout.data_list_item, null);
            }


            cameraBtn = (ImageButton) convertView.findViewById(R.id.CamBtn);
            cameraBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    mChart.saveToGallery(graphName.get(position),50);
                    Toast.makeText(getActivity().getApplicationContext(), "Graph " + "\"" + graphName.get(position) + "\"" + " saved to Gallery!", Toast.LENGTH_LONG).show();
                }
            });

            dataNameText = (TextView) convertView.findViewById(R.id.dataNameTextView);
            deleteBtn = (ImageButton) convertView.findViewById(R.id.DeleteBtn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            getActivity());
                    alertDialogBuilder
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this entry?")
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    graphName.remove(position);

                                    displayFragInterface.getSavedGraphDataList().remove(position);

                                    // Save the updated graph list to internal storage
                                    graphFileStorage.saveGraphFile(getActivity(),displayFragInterface.getSavedGraphDataList());

                                    if (selectedPosition == position) {
                                        mChart.clear();
                                        selectedPosition = -1;
                                    }

                                    if (position < selectedPosition)
                                        --selectedPosition;

                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
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

            if (position == selectedPosition) {
                convertView.setBackgroundColor(SensorConst.SELECTION_COLOR);
                cameraBtn.setVisibility(View.VISIBLE);
            }
            else {
                convertView.setBackgroundColor(Color.WHITE);
                cameraBtn.setVisibility(View.INVISIBLE);
            }

            if (dataNameText != null)
                dataNameText.setText(graphName.get(position));

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    addEntry(displayFragInterface.getSavedGraphDataList().get(position).getData(), position);
                  //  Log.i(TAG, "Graph List Item clicked");
                    selectedPosition = position;
                    notifyDataSetChanged();
                }
            });

            return convertView;

        }
    }
}
