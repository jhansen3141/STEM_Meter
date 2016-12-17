package com.example.josh.boardtestx;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.Handler;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ConnectFragment.ConnectFragInterface, SensorsFragment.SensorFragInterface, GraphFragment.GraphFragInterface {

    private String TAG = "MainActivity";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
    public final static String DEVICE_MAC_STR = "CC:78:AB:19:9A:31";

    public final static UUID BOARD_UUID = UUID.fromString("0000ABAE-0000-1000-8000-00805F9B34FB");
    public final static UUID SENSOR_1_DATA_UUID = UUID.fromString("F000BEAA-0451-4000-B000-000000000000");
    public final static UUID SENSOR_2_DATA_UUID = UUID.fromString("F000BEAB-0451-4000-B000-000000000000");
    public final static UUID SENSOR_3_DATA_UUID = UUID.fromString("F000BEAC-0451-4000-B000-000000000000");
    public final static UUID SENSOR_4_DATA_UUID = UUID.fromString("F000BEAD-0451-4000-B000-000000000000");
    public final static UUID SENSOR_1_CONFIG_UUID = UUID.fromString("F000BEAE-0451-4000-B000-000000000000");
    public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic BoardSensor1DataChar;
    private BluetoothGattCharacteristic BoardSensor2DataChar;
    private BluetoothGattCharacteristic BoardSensor3DataChar;
    private BluetoothGattCharacteristic BoardSensor4DataChar;
    private BluetoothGattCharacteristic BoardSensor1ConfigChar;
    private BluetoothGattService BoardService;

    private BluetoothDevice boardDevice;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private Queue<BluetoothGattDescriptor> gattDescriptors;

    private Handler mHandler;
    private boolean writeFinished = false;
    private boolean serviceDiscovered = false;
    private boolean mScanning = false;

    private int mConnectionState = STATE_DISCONNECTED;
    private int FAILURE = 0;
    private int SUCCESS = 1;

    private final String CONNECT_FRAG_TAG = "ConnectFragTag";
    private final String GRAPH_FRAG_TAG = "GraphFragTag";
    private final String SENSOR_FRAG_TAG = "SensorFragTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //noinspection RestrictedApi
        navigationView.setNavigationItemSelectedListener(this);

        gattDescriptors = new LinkedList<>();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device does not support BLE", Toast.LENGTH_SHORT).show();
            finish();
        }

        ConnectFragment connectFragment = new ConnectFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, connectFragment, CONNECT_FRAG_TAG).commit();

    }

    public void BoardConnect() {
        mBluetoothManager = (BluetoothManager) getSystemService(getApplicationContext().BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) { // if bluetooth is not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            printConnectionStat("Disconnected");
        } else {
            boardDevice = mBluetoothAdapter.getRemoteDevice(DEVICE_MAC_STR);
            if (boardDevice == null) {
                printConnectionStat("Scanning for board...");
                scanLeDevice(true);
            } else {
                printConnectionStat("Connecting to board...");
                mBluetoothGatt = boardDevice.connectGatt(getApplicationContext(), true, mGattCallback);
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        Log.i(TAG, "Scanning...");
        final long SCAN_PERIOD = 10000;
        mHandler = new Handler();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            Log.i(TAG, "Scanning...");

        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (device.getName().equals("STEM Meter")) {
                        boardDevice = device;
                        mBluetoothGatt = boardDevice.connectGatt(getApplicationContext(), true, mGattCallback);
                    }
                    printConnectionStat("Board Found");
                    Log.i(TAG, "DEVICE FOUND: " + device.getName());
                }
            };

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    Log.i(TAG, "Des Written " + descriptor + " " + status);
                    if (!gattDescriptors.isEmpty()) {
                        gatt.writeDescriptor(gattDescriptors.remove());
                    }
                }

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        mConnectionState = STATE_CONNECTED;
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
                        printConnectionStat("Connected to GATT server. Starting service discovery");

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        printConnectionStat("Disconnected from GATT server");
                    }
                }

                @Override
                // Services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "Services Discovered");
                        printConnectionStat("Services Discovered");
                        BoardService = mBluetoothGatt.getService(BOARD_UUID);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        BoardSensor1DataChar = BoardService.getCharacteristic(SENSOR_1_DATA_UUID); // get the SMS characteristic
                        BoardSensor2DataChar = BoardService.getCharacteristic(SENSOR_2_DATA_UUID); // get the SMS characteristic
                        BoardSensor3DataChar = BoardService.getCharacteristic(SENSOR_3_DATA_UUID); // get the SMS characteristic
                        BoardSensor4DataChar = BoardService.getCharacteristic(SENSOR_4_DATA_UUID); // get the Time characteristic
                        BoardSensor1ConfigChar = BoardService.getCharacteristic(SENSOR_1_CONFIG_UUID);

                        // Enable Sensor 1 Char Notifications
                        gatt.setCharacteristicNotification(BoardSensor1DataChar, true);
                        BluetoothGattDescriptor sensor1Desc = BoardSensor1DataChar.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                        sensor1Desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(sensor1Desc);

                        // Enable Sensor 2 Char Notifications
                        gatt.setCharacteristicNotification(BoardSensor2DataChar, true);
                        BluetoothGattDescriptor sensor2Desc = BoardSensor2DataChar.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                        sensor2Desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gattDescriptors.add(sensor2Desc); // have to add it to a queue to get them to write correctly. Written in callback

                        // Enable Sensor 3 Char Notifications
                        gatt.setCharacteristicNotification(BoardSensor3DataChar, true);
                        BluetoothGattDescriptor sensor3Desc = BoardSensor3DataChar.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                        sensor3Desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gattDescriptors.add(sensor3Desc);

                        // Enable Sensor 4 Char Notifications
                        gatt.setCharacteristicNotification(BoardSensor4DataChar, true);
                        BluetoothGattDescriptor sensor4Desc = BoardSensor4DataChar.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID);
                        sensor4Desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gattDescriptors.add(sensor4Desc);

                        serviceDiscovered = true;
                        printConnectionStat("Connected");

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        SensorsFragment sensorsFragment = new SensorsFragment();
                        transaction.replace(R.id.fragment_container, sensorsFragment, SENSOR_FRAG_TAG);
                        // transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {

                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    writeFinished = true;
                    Log.i(TAG, "CHAR WRITE FINISHED");
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    if (characteristic.equals(BoardSensor1DataChar)) {
                        handleSensor1Data(characteristic.getValue());
                    } else if (characteristic.equals(BoardSensor2DataChar)) {
                        handleSensor2Data(characteristic.getValue());
                    } else if (characteristic.equals(BoardSensor3DataChar)) {
                        handleSensor3Data(characteristic.getValue());
                    } else if (characteristic.equals(BoardSensor4DataChar)) {
                        handleSensor4Data(characteristic.getValue());
                    }

                }
            };

    public void printConnectionStat(String string) {
        ConnectFragment connectFragment = (ConnectFragment)
                getSupportFragmentManager().findFragmentByTag(CONNECT_FRAG_TAG);
        if (connectFragment != null) {
            connectFragment.printConnectionStatus(string);
        }
    }

    public void addToGraph(float data) {
        GraphFragment graphFragment = (GraphFragment)
                getSupportFragmentManager().findFragmentByTag(GRAPH_FRAG_TAG);
        if (graphFragment != null) {
            graphFragment.addPlot(data);
        }
    }

    SensorData getSensorData(int sensorNum, int sensorType, byte sensorData[]) {
        String finalString = "";
        float graphData = 0;
        switch (sensorType) {
            case SensorList.IMU_MPU6050:
                IMU_MPU6050 imuSensor = new IMU_MPU6050(sensorData);
                imuSensor.calcSensorData(); // calc data
                finalString = imuSensor.toString();
                graphData = imuSensor.getxAccelF();
                break;
            case SensorList.TEMP_MCP9808:
                TEMP_MCP9808 tempSensor = new TEMP_MCP9808(sensorData);
                tempSensor.calcSensorData();
                finalString = tempSensor.toString();
                graphData = tempSensor.getTempF();
                break;
        }
        return new SensorData(sensorNum, finalString, graphData);
    }

    void handleSensor1Data(byte sensor1Data[]) {
        SensorData sensorData;
        if (sensor1Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            switch (sensor1Data[0]) {
                case SensorList.IMU_MPU6050:
                    sensorData = getSensorData(1, SensorList.IMU_MPU6050, sensor1Data);
                    break;
                case SensorList.TEMP_MCP9808:
                    sensorData = getSensorData(1, SensorList.TEMP_MCP9808, sensor1Data);
                    break;
                default:
                    return;
            }
        }
        postSensorData(sensorData);
    }

    void handleSensor2Data(byte sensor2Data[]) {
        SensorData sensorData;
        if (sensor2Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            switch (sensor2Data[0]) {
                case SensorList.IMU_MPU6050:
                    sensorData = getSensorData(2, SensorList.IMU_MPU6050, sensor2Data);
                    break;
                case SensorList.TEMP_MCP9808:
                    sensorData = getSensorData(2, SensorList.TEMP_MCP9808, sensor2Data);
                    break;
                default:
                    return;
            }
        }
        postSensorData(sensorData);

    }

    void handleSensor3Data(byte sensor3Data[]) {
        SensorData sensorData;
        if (sensor3Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            switch (sensor3Data[0]) {
                case SensorList.IMU_MPU6050:
                    sensorData = getSensorData(3, SensorList.IMU_MPU6050, sensor3Data);
                    break;
                case SensorList.TEMP_MCP9808:
                    sensorData = getSensorData(3, SensorList.TEMP_MCP9808, sensor3Data);
                    break;
                default:
                    return;

            }
        }
        postSensorData(sensorData);

    }

    void handleSensor4Data(byte sensor4Data[]) {
        SensorData sensorData;
        if (sensor4Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            switch (sensor4Data[0]) {
                case SensorList.IMU_MPU6050:
                    sensorData = getSensorData(4, SensorList.IMU_MPU6050, sensor4Data);
                    break;
                case SensorList.TEMP_MCP9808:
                    sensorData = getSensorData(4, SensorList.TEMP_MCP9808, sensor4Data);
                    break;
                default:
                    return;

            }
        }
        postSensorData(sensorData);

    }

    public void postSensorData(SensorData sensorData) {
        SensorsFragment sensorsFragment = (SensorsFragment)
                getSupportFragmentManager().findFragmentByTag(SENSOR_FRAG_TAG);

        if (sensorsFragment != null && sensorsFragment.isVisible()) {
            sensorsFragment.printSensorData(sensorData.getSensorNum(), sensorData.getStrData());
        } else if(sensorData.getSensorNum() == 4) {
            GraphFragment graphFragment = (GraphFragment)
                    getSupportFragmentManager().findFragmentByTag(GRAPH_FRAG_TAG);
            if (graphFragment != null && graphFragment.isVisible()) {
                addToGraph(sensorData.getGraphData());
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this,"Josh Hansen\n " +
                    " CEEN 4360 \n " +
                    "    Phase II",Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (id == R.id.nav_connect) {
            ConnectFragment connectFragment = new ConnectFragment();
            transaction.replace(R.id.fragment_container, connectFragment, CONNECT_FRAG_TAG);
            //transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_sensors) {
            SensorsFragment sensorsFragment = new SensorsFragment();
            transaction.replace(R.id.fragment_container, sensorsFragment, SENSOR_FRAG_TAG);
            // transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_graph) {
            GraphFragment graphFragment = new GraphFragment();
            transaction.replace(R.id.fragment_container, graphFragment, GRAPH_FRAG_TAG);
            //transaction.addToBackStack(null);
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void ledToggle(boolean ledON) {
        if (ledON) {
            writeCharacteristic(BoardSensor1ConfigChar, "0");
        } else {
            writeCharacteristic(BoardSensor1ConfigChar, "1");
        }
    }

    public int writeCharacteristic(BluetoothGattCharacteristic characteristic, String data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return FAILURE;
        }

        if ((data.length() != 20)) {
            data = padRight(data, 20);
        }

        try {
            Log.i(TAG, "Sending: " + data);
            characteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (NullPointerException npe) {
            return FAILURE;
        }

        return waitForWrite();
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    int waitForWrite() {
        long end = System.currentTimeMillis() + 5 * 1000; // 2 second timeout
        Log.i(TAG, "Waiting for write to complete");
        while (!writeFinished) {
            if (System.currentTimeMillis() > end) {
                Log.e(TAG, "Write Timeout");
                return FAILURE;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        writeFinished = false;
        Log.i(TAG, "Write complete");

        return SUCCESS;
    }
}

class SensorData {
    private String strData;
    private float graphData;
    private int sensorNum;
    public SensorData(int sensorNum, String strData, float graphData) {
        this.strData = strData;
        this.graphData = graphData;
        this.sensorNum = sensorNum;
    }

    public String getStrData() {
        return strData;
    }

    public float getGraphData() {
        return graphData;
    }

    public int getSensorNum() {
        return sensorNum;
    }
}
