package com.stemmeter.stem_meter;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Josh on 3/2/2017.
 */
public class BLEDevice {

    private BluetoothDevice bluetoothDevice;
    private String RSSIStr;

    public BLEDevice(BluetoothDevice device, String RSSI) {
        this.bluetoothDevice = device;
        this.RSSIStr = RSSI + " dBm";
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

    public void setRSSIStr(String RSSIStr) {
        this.RSSIStr = RSSIStr;
    }
}
