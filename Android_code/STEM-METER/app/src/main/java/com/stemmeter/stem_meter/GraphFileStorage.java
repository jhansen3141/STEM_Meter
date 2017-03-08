package com.stemmeter.stem_meter;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Josh on 3/8/2017.
 */
public class GraphFileStorage {

    private static final String fileName = "SMGraphFiles";
    private String TAG = "GraphFileStorage";
    public GraphFileStorage() {}

    public boolean saveGraphFile(Context context, ArrayList<SavedGraphData> savedGraphData) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(savedGraphData);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "File not found while writing");
            return false;
        } catch (IOException e) {
            Log.i(TAG, "IO exception while writing");
            return false;
        }
        return true;
    }

    public ArrayList<SavedGraphData> readGraphFile(Context context) {
        ArrayList<SavedGraphData> graphDataList = null;

        try {
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            graphDataList = (ArrayList<SavedGraphData>) is.readObject();
            is.close();
            fis.close();

        } catch (ClassNotFoundException e) {
            Log.i(TAG, "Class not found while reading");
            return null;
        } catch (FileNotFoundException e) {
            Log.i(TAG, "File not found while reading");
            return null;
        } catch (IOException e) {
            Log.i(TAG, "IO Exception while reading");
            return null;
        }


        return graphDataList;
    }

}
