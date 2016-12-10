package com.example.josh.boardtestx;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * Created by Josh on 11/28/2016.
 */

public class ConnectFragment extends Fragment {
    private String TAG = "ConnectFragment";
    ConnectFragInterface connectFragInterface;
    private Button connectButton;
    private TextView connectTextView;

    // Container Activity must implement this interface
    public interface ConnectFragInterface {
        public void BoardConnect();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG,"Connect button pressed");
                connectFragInterface.BoardConnect();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.connect_fragment, container, false);
        connectButton = (Button)view.findViewById(R.id.connectButton);
        connectTextView = (TextView)view.findViewById(R.id.connectTextView);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            connectFragInterface = (ConnectFragInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ConnectFragInterface");
        }
    }

    public void printConnectionStatus(final String string) {
        connectTextView.post(
                new Runnable() {
                    public void run() {

                        connectTextView.setText(string);
                    }
                });
    }
}
