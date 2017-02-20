package com.stemmeter.stem_meter;

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
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.stemmeter.stem_meter.Sensors.IMU_MPU6050;
import com.stemmeter.stem_meter.Sensors.Sensor;
import com.stemmeter.stem_meter.Sensors.TEMP_MCP9808;

import java.util.Calendar;
import java.util.GregorianCalendar;
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
    public final static UUID SENSOR_CONFIG_UUID = UUID.fromString("F000BEAF-0451-4000-B000-000000000000");
    public final static UUID BATTERY_INFO_UUID = UUID.fromString("F000BEBC-0451-4000-B000-000000000000");
    public final static UUID TIME_CONFIG_UUID = UUID.fromString("F000BEBD-0451-4000-B000-000000000000");
    public static final UUID CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic BoardSensor1DataChar;
    private BluetoothGattCharacteristic BoardSensor2DataChar;
    private BluetoothGattCharacteristic BoardSensor3DataChar;
    private BluetoothGattCharacteristic BoardSensor4DataChar;

    private BluetoothGattCharacteristic BoardBatteryInfoChar;
    private BluetoothGattCharacteristic BoardSensorConfigChar;
    private BluetoothGattCharacteristic BoardTimeConfigChar;
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

    private Sensor sensor1;
    private Sensor sensor2;
    private Sensor sensor3;
    private Sensor sensor4;

    private SensorConfig sensorConfig1;
    private SensorConfig sensorConfig2;
    private SensorConfig sensorConfig3;
    private SensorConfig sensorConfig4;

    private GraphConfig graphConfig;

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

        sensorConfig1 = new SensorConfig(1);
        sensorConfig2 = new SensorConfig(2);
        sensorConfig3 = new SensorConfig(3);
        sensorConfig4 = new SensorConfig(4);

        graphConfig = new GraphConfig();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //noinspection RestrictedApi
        navigationView.setNavigationItemSelectedListener(this);

        gattDescriptors = new LinkedList<>();
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
            // TODO change this from hard-coded MAC address to name of BLE device or let user choose from list
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
                        BoardSensor1DataChar = BoardService.getCharacteristic(SENSOR_1_DATA_UUID); // get sensor 1 char
                        BoardSensor2DataChar = BoardService.getCharacteristic(SENSOR_2_DATA_UUID);// get sensor 2 char
                        BoardSensor3DataChar = BoardService.getCharacteristic(SENSOR_3_DATA_UUID); /// get sensor 3 char
                        BoardSensor4DataChar = BoardService.getCharacteristic(SENSOR_4_DATA_UUID); // get sensor 4 char
                        BoardSensorConfigChar = BoardService.getCharacteristic(SENSOR_CONFIG_UUID); // get the sensor config char
                        BoardBatteryInfoChar = BoardService.getCharacteristic(BATTERY_INFO_UUID); // get the battery info char

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

                        // At this point we are connected and all chars have been assigned
                        // Set the time:
                        setBaseUnitTime();

                        // Read the current config values from base unit
                        // Once read callback will update config objects
                        mBluetoothGatt.readCharacteristic(BoardSensorConfigChar);

                    } else {
                        Log.w(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if(characteristic.equals(BoardSensorConfigChar)) {
                            // Sensor config was read from base unit so update the config objects
                            updateSensorConfigData(characteristic.getValue());
                        }
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

    // method to update sensor config objects using byte array data read from base unit
    public void updateSensorConfigData(byte configData[]) {
        // Byte 0 = S1 Freq | Byte 1 = S1 SD Log
        // Byte 2 = S2 Freq | Byte 3 = S2 SD Log
        // Byte 4 = S3 Freq | Byte 5 = S3 SD Log
        // Byte 6 = S4 Freq | Byte 7 = S4 SD Log

        sensorConfig1.setFreq((int)configData[0]);
        sensorConfig1.setSDLogging(configData[1] == 1);

        sensorConfig2.setFreq((int)configData[1]);
        sensorConfig2.setSDLogging(configData[2] == 1);

        sensorConfig3.setFreq((int)configData[3]);
        sensorConfig3.setSDLogging(configData[4] == 1);

        sensorConfig4.setFreq((int)configData[5]);
        sensorConfig4.setSDLogging(configData[6] == 1);
    }

    public void setBaseUnitTime() {
        Calendar calendar = new GregorianCalendar();
        byte month = (byte)calendar.get(Calendar.MONTH);
        byte day = (byte)calendar.get(Calendar.DAY_OF_MONTH);
        byte year = (byte)(calendar.get(Calendar.YEAR)-200);
        byte dow = (byte)(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        byte hour = (byte)calendar.get(Calendar.HOUR_OF_DAY);
        byte minutes = (byte)calendar.get(Calendar.MINUTE);
        byte seconds = (byte)calendar.get(Calendar.SECOND);

        // Byte[0] = dow
        // Byte[1] = day
        // Byte[2] = month
        // Byte[3] = year
        // Byte[4] = hour
        // Byte[5] = min
        // Byte[6] = sec

        byte[] timeData = new byte[7];
        timeData[0] = dow;
        timeData[1] = day;
        timeData[2] = month;
        timeData[3] = year;
        timeData[4] = hour;
        timeData[5] = minutes;
        timeData[6] = seconds;

        writeCharacteristic(BoardSensorConfigChar, timeData);
    }

    void handleSensor1Data(byte sensor1Data[]) {
        Log.i(TAG, "HANDLE S1");
        if (sensor1Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            switch (sensor1Data[0]) {
                case SensorList.IMU_MPU6050:
                    // check to see if sensor is already "installed"
                    if(sensor1 == null || !(sensor1 instanceof IMU_MPU6050)) {
                        sensor1 = new IMU_MPU6050(sensor1Data, 1);
                    }
                    break;
                case SensorList.TEMP_MCP9808:
                    // check to see if sensor is already "installed"
                    if(sensor1 == null || !(sensor1 instanceof TEMP_MCP9808)) {
                        sensor1 = new TEMP_MCP9808(sensor1Data, 1);
                    }
                    break;
                default:
                    // if invalid sensor type then return
                    return;
            }
        }
        // update the sensor data
        sensor1.updateData(sensor1Data);
        // perform calculations on data
        sensor1.calcSensorData();
        // post the data to the screen
        postSensorData(sensor1);
    }

    void handleSensor2Data(byte sensor2Data[]) {
        Log.i(TAG,"HANDLE S2");
        if (sensor2Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            switch (sensor2Data[0]) {
                case SensorList.IMU_MPU6050:
                    // check to see if sensor is already "installed"
                    if(sensor2 == null || !(sensor2 instanceof IMU_MPU6050)) {
                        sensor2 = new IMU_MPU6050(sensor2Data, 2);
                    }
                    break;
                case SensorList.TEMP_MCP9808:
                    // check to see if sensor is already "installed"
                    if(sensor2 == null || !(sensor2 instanceof TEMP_MCP9808)) {
                        sensor2 = new TEMP_MCP9808(sensor2Data, 1);
                    }
                    break;
                default:
                    // if invalid sensor type then return
                    return;
            }
        }
        // update the sensor data
        sensor2.updateData(sensor2Data);
        // perform calculations on data
        sensor2.calcSensorData();
        // post the data to the screen
        postSensorData(sensor2);

    }

    void handleSensor3Data(byte sensor3Data[]) {
        Log.i(TAG,"HANDLE S3");
        if (sensor3Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            switch (sensor3Data[0]) {
                case SensorList.IMU_MPU6050:
                    // check to see if sensor is already "installed"
                    if(sensor3 == null || !(sensor3 instanceof IMU_MPU6050)) {
                        sensor3 = new IMU_MPU6050(sensor3Data, 3);
                    }
                    break;
                case SensorList.TEMP_MCP9808:
                    // check to see if sensor is already "installed"
                    if(sensor3 == null || !(sensor3 instanceof TEMP_MCP9808)) {
                        sensor3 = new TEMP_MCP9808(sensor3Data, 3);
                    }
                    break;
                default:
                    // if invalid sensor type then return
                    return;
            }
        }
        // update the sensor data
        sensor3.updateData(sensor3Data);
        // perform calculations on data
        sensor3.calcSensorData();
        // post the data to the screen
        postSensorData(sensor3);
    }

    void handleSensor4Data(byte sensor4Data[]) {
        Log.i(TAG,"HANDLE S4");
        if (sensor4Data[0] == SensorList.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            switch (sensor4Data[0]) {
                case SensorList.IMU_MPU6050:
                    // check to see if sensor is already "installed"
                    if(sensor4 == null || !(sensor4 instanceof IMU_MPU6050)) {
                        sensor4 = new IMU_MPU6050(sensor4Data, 4);
                    }
                    break;
                case SensorList.TEMP_MCP9808:
                    // check to see if sensor is already "installed"
                    if(sensor4 == null || !(sensor4 instanceof TEMP_MCP9808)) {
                        sensor4 = new TEMP_MCP9808(sensor4Data, 4);
                    }
                    break;
                default:
                    // if invalid sensor type then return
                    return;
            }
        }
        // update the sensor data
        sensor4.updateData(sensor4Data);
        // perform calculations on data
        sensor4.calcSensorData();
        // post the data to the screen
        postSensorData(sensor4);
    }

    public void postSensorData(Sensor sensor) {
        SensorsFragment sensorsFragment = (SensorsFragment)
                getSupportFragmentManager().findFragmentByTag(SENSOR_FRAG_TAG);
        // if the sensor fragment is showing print the data there
        if (sensorsFragment != null && sensorsFragment.isVisible()) {
            sensorsFragment.printSensorData(sensor.getSensorNumber(), sensor.toString());
    }
        // Determines whether to add sensor data to graph
        else if(sensor.getSensorNumber() == graphConfig.getSelectedSensor()) {
            GraphFragment graphFragment = (GraphFragment)
                    getSupportFragmentManager().findFragmentByTag(GRAPH_FRAG_TAG);
            if (graphFragment != null && graphFragment.isVisible()) {
                addToGraph(sensor.getGraphData());
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
            // TODO Do something when user clicks settings button
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
            //transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_graph) {
            GraphFragment graphFragment = new GraphFragment();
            transaction.replace(R.id.fragment_container, graphFragment, GRAPH_FRAG_TAG);
            //transaction.addToBackStack(null);
            transaction.commit();
        } else if (id == R.id.nav_display) {
            DisplayFragment displayFragment = new DisplayFragment();
            transaction.replace(R.id.fragment_container, displayFragment, GRAPH_FRAG_TAG);
            //transaction.addToBackStack(null);
            transaction.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean sensorConfigWrite(SensorConfig config) {

        switch(config.getSensorNumber()) {
            case SensorList.SENSOR_1:
                sensorConfig1.setFreq(config.getFreq());
                sensorConfig1.setSDLogging(config.isSDLogging());
                break;
            case SensorList.SENSOR_2:
                sensorConfig2.setFreq(config.getFreq());
                sensorConfig2.setSDLogging(config.isSDLogging());
                break;
            case SensorList.SENSOR_3:
                sensorConfig3.setFreq(config.getFreq());
                sensorConfig3.setSDLogging(config.isSDLogging());
                break;
            case SensorList.SENSOR_4:
                sensorConfig4.setFreq(config.getFreq());
                sensorConfig4.setSDLogging(config.isSDLogging());
                break;
        }

        // Byte 0 = S1 Freq | Byte 1 = S1 SD Log
        // Byte 2 = S2 Freq | Byte 3 = S2 SD Log
        // Byte 4 = S3 Freq | Byte 5 = S3 SD Log
        // Byte 6 = S4 Freq | Byte 7 = S4 SD Log

        byte[] configData = new byte[8];

        configData[0] = (byte)sensorConfig1.getFreq();
        configData[1] = (byte)((sensorConfig1.isSDLogging()) ? 1 : 0);

        configData[2] = (byte)sensorConfig2.getFreq();
        configData[3] = (byte)((sensorConfig2.isSDLogging()) ? 1 : 0);

        configData[4] = (byte)sensorConfig3.getFreq();
        configData[5] = (byte)((sensorConfig3.isSDLogging()) ? 1 : 0);

        configData[6] = (byte)sensorConfig4.getFreq();
        configData[5] = (byte)((sensorConfig4.isSDLogging()) ? 1 : 0);

        return writeCharacteristic(BoardSensorConfigChar, configData);
    }

    @Override
    public SensorConfig getSensorConfig(int sensorNumber) {
        SensorConfig config = null;
        switch(sensorNumber) {
            case 1:
                config = sensorConfig1;
            break;
            case 2:
                config = sensorConfig2;
            break;
            case 3:
                config = sensorConfig3;
                break;
            case 4:
                config = sensorConfig4;
            break;

        }

        return config;
    }

    @Override
    public GraphConfig getGraphConfig(){

        return graphConfig;
    }

    // Overloaded method to write char data in byte array form
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data) {
        byte[] dataToSend = new byte[20];

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        for(int i=0;i<20;i++) {
            dataToSend[i] = 0;
        }

        for(int i=0;i<data.length;i++) {
            dataToSend[i] = data[i];
        }

        try {
            Log.i(TAG, "Sending: " + dataToSend[0] + " " + dataToSend[1] + " " + dataToSend[2]);
            characteristic.setValue(dataToSend);
            mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (NullPointerException npe) {
            return false;
        }
        return waitForWrite();
    }

    // Overloaded method to write char data in string (ASCII) form
    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic, String data) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        if ((data.length() != 20)) {
            data = padRight(data, 20);
        }

        try {
            Log.i(TAG, "Sending: " + data);
            characteristic.setValue(data);
            mBluetoothGatt.writeCharacteristic(characteristic);
        } catch (NullPointerException npe) {
            return false;
        }

        return waitForWrite();
    }

    public static String padRight(String s, int n) {
        return String.format("%1$-" + n + "s", s);
    }

    boolean waitForWrite() {
        long end = System.currentTimeMillis() + 5 * 1000; // 2 second timeout
        Log.i(TAG, "Waiting for write to complete");
        while (!writeFinished) {
            if (System.currentTimeMillis() > end) {
                Log.e(TAG, "Write Timeout");
                return false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        writeFinished = false;
        Log.i(TAG, "Write complete");

        return true;
    }
}