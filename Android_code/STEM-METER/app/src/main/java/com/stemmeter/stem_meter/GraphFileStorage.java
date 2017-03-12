package com.stemmeter.stem_meter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Josh on 3/8/2017.
 */
public class GraphFileStorage implements Serializable {

    private static final String fileName = "SMGraphFiles";
    private String TAG = "GraphFileStorage";
    private ArrayList<Integer> colorList;
    static final long serialVersionUID =8106084045538222391L;

    public GraphFileStorage() {
        colorList = new ArrayList<>();
        // If a sensor is added that has more than 5 graphable data
        // points, more colors need to be added
        colorList.add(Color.RED);
        colorList.add(Color.BLUE);
        colorList.add(Color.BLACK);
        colorList.add(Color.GREEN);
        colorList.add(Color.CYAN);

    }

    public boolean saveGraphFile(Context context, ArrayList<SavedGraphData> savedGraphData) {
        ArrayList<ArrayList<GraphPlot>> graphFilesList = serializeGraphData(savedGraphData);
        FileOutputStream fos;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            Log.i(TAG,"Writing graph files to internal storage: " + graphFilesList.size());
            os.writeObject(graphFilesList);
            os.close();
            fos.close();
            Log.i(TAG,"File Write Complete");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "File not found while writing: " + e);
            return false;
        } catch (IOException e) {
            Log.i(TAG, "IO exception while writing: " + e);
            return false;
        }
        return true;
    }

    public ArrayList<SavedGraphData> readGraphFiles(Context context) {
        ArrayList<ArrayList<GraphPlot>> graphFilesList;
        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            Log.i(TAG,"Reading graph files from internal storage");
            graphFilesList = (ArrayList<ArrayList<GraphPlot>>) is.readObject();
            is.close();
            fis.close();
            Log.i(TAG,"File Read Complete: " + graphFilesList.size());

        } catch (ClassNotFoundException e) {
            Log.i(TAG, "Class not found while reading: " + e);
            return null;
        } catch (FileNotFoundException e) {
            Log.i(TAG, "File not found while reading: " + e);
            return null;
        } catch (IOException e) {
            Log.i(TAG, "IO Exception while reading: " + e);
            return null;
        }


        return unserializeGraphData(graphFilesList);

    }

    private ArrayList<ArrayList<GraphPlot>> serializeGraphData(ArrayList<SavedGraphData> savedGraphData) {
        // A list of graph file lists
        ArrayList<ArrayList<GraphPlot>> graphFileListofLists = new ArrayList<>();

        for(SavedGraphData graphData : savedGraphData) {
            ArrayList<GraphPlot> graphFileList = new ArrayList<>();
            // Go through all the data sets in the LineData object
            for(int i=0; i<graphData.getData().getDataSetCount();i++) {
                String plotString = graphData.getData().getDataSetByIndex(i).getLabel();
                GraphPlot newGraphPlot = new GraphPlot(plotString, graphData.getName(), graphData.getRate(),graphData.getUnits());

                // Go through all entries for this data set
                for(int j=0; j<graphData.getData().getDataSetByIndex(i).getEntryCount(); j++) {
                    // Assign the X and Y float values to a new GraphEntry object
                    GraphEntry graphEntry = new GraphEntry(graphData.getData().getDataSetByIndex(i).getEntryForIndex(j).getX(),
                                                graphData.getData().getDataSetByIndex(i).getEntryForIndex(j).getY());

                    // Add new graph entry to list
                    newGraphPlot.addGraphEntry(graphEntry);

                }
                // Add new plot to list
                graphFileList.add(newGraphPlot);
            }
            graphFileListofLists.add(graphFileList);
        }

        return graphFileListofLists;
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

    private ArrayList<SavedGraphData> unserializeGraphData( ArrayList<ArrayList<GraphPlot>> graphFilesList) {
        ArrayList<SavedGraphData> savedGraphDataList = new ArrayList<>();
        // Go through each graph
        for(int i=0;i<graphFilesList.size();i++) {
            LineData lineData = new LineData();
            GraphPlot graphPlot = null;
            for(int j=0;j<graphFilesList.get(i).size();j++) {
                graphPlot = graphFilesList.get(i).get(j);
                ILineDataSet set = createSet(colorList.get(j), colorList.get(j), graphPlot.getName());
                lineData.addDataSet(set);
                // Go through all entries in the Graph File
                for(int k=0;k<graphPlot.getGraphEntries().size();k++) {
                    lineData.addEntry(new Entry(graphPlot.getGraphEntries().get(k).getX(),graphPlot.getGraphEntries().get(k).getY()), j);
                }
            }
            if(graphPlot != null) {
                savedGraphDataList.add(new SavedGraphData(graphPlot.getFileName(), lineData, graphPlot.getRate(), graphPlot.getUnits()));
            }
        }

        return savedGraphDataList;
    }

    private class GraphPlot implements Serializable {
        static final long serialVersionUID =8106084045538222391L;
        ArrayList<GraphEntry> graphEntries;
        private String fileName;
        private String name;
        private int rate;
        private int units;


        private GraphPlot(String name,String fileName, int rate, int units) {
            graphEntries = new ArrayList<>();
            this.fileName = fileName;
            this.name = name;
            this.rate = rate;
            this.units = units;
        }

        private void addGraphEntry(GraphEntry dataPoint) {
            graphEntries.add(dataPoint);
        }

        private ArrayList<GraphEntry> getGraphEntries() {
            return graphEntries;
        }

        public String getName() {
            return name;
        }

        public int getRate() {
            return rate;
        }

        public int getUnits() {
            return units;
        }

        public String getFileName() {
            return fileName;
        }
    }


    private class GraphEntry implements Serializable {
        static final long serialVersionUID =8106084045538222391L;
        float x, y;

        private GraphEntry(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getY() {
            return y;
        }

        public float getX() {
            return x;
        }
    }

}
