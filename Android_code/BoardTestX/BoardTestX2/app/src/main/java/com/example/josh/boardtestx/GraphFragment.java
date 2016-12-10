package com.example.josh.boardtestx;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Josh on 11/28/2016.
 */

public class GraphFragment extends Fragment {



    // Container Activity must implement this interface
    public interface GraphFragInterface {

    }

    GraphFragInterface graphFragInterface;
    private String TAG = "GraphFragment";
    private XYPlot plot;;
    private DynamicSeries series;
    private LineAndPointFormatter seriesFormatter;
    private long currentIndex = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.graph_fragment, container, false);
        plot = (XYPlot) view.findViewById(R.id.plot);
        series = new DynamicSeries("Acceleration");

        seriesFormatter = new LineAndPointFormatter();
        seriesFormatter.setPointLabelFormatter(new PointLabelFormatter());
        seriesFormatter.configure(getActivity().getApplicationContext(),
                R.xml.line_point_formatter_with_labels);
        seriesFormatter.setPointLabelFormatter(new PointLabelFormatter(Color.TRANSPARENT));

        plot.addSeries(series,seriesFormatter);

        plot.setDomainBoundaries(0, 300, BoundaryMode.FIXED );
        plot.setRangeBoundaries(-3,3,BoundaryMode.FIXED);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        plot.redraw();
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

    class DynamicSeries implements XYSeries {

        private String title;
        ArrayList<Float> dataValues;

        public DynamicSeries(String title) {
            dataValues = new ArrayList<>();
            this.title = title;
        }

        public void add(float data) {
            dataValues.add(data);
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int size() {
            return dataValues.size();
        }

        @Override
        public Number getX(int index) {
            //Log.i(TAG,"X: " + index);
            return index;
        }

        @Override
        public Number getY(int index) {
            //Log.i(TAG,"Y: " + dataValues.get(index));
            return dataValues.get(index);
        }

        public void clear() {
            dataValues.clear();
        }
    }

    public void addPlot(float value) {
        int seriesSize = series.size();
        if(seriesSize > 300) {
            plot.setDomainBoundaries(seriesSize-300, seriesSize, BoundaryMode.FIXED);
        }
        series.add(value);
        plot.redraw();

    }
}
