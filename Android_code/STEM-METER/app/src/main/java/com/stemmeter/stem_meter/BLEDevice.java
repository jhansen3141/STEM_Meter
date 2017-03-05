package com.stemmeter.stem_meter;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Josh on 3/2/2017.
 */
public class BLEDevice {

    private BluetoothDevice bluetoothDevice;
    private String RSSIStr;
    private int rssi;

    public BLEDevice(BluetoothDevice device, int RSSI) {
        this.bluetoothDevice = device;
        this. rssi = RSSI;
        this.RSSIStr = " " + Integer.toString(rssi) + "\ndBm";
    }

    public BLEDevice(){}

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getRSSIStr() {
        return RSSIStr;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRSSIStr(String RSSIStr) {
        this.RSSIStr = RSSIStr;
    }
}
