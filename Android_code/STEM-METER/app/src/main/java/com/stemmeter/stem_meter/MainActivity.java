package com.stemmeter.stem_meter;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.github.mikephil.charting.data.LineData;
import com.stemmeter.stem_meter.Sensors.Accel_MPU6050;
import com.stemmeter.stem_meter.Sensors.Gyro_MPU6050;
import com.stemmeter.stem_meter.Sensors.LIGHT_OPT3002;
import com.stemmeter.stem_meter.Sensors.MAG_MAG3110;
import com.stemmeter.stem_meter.Sensors.PRESSURE_MPL3115A2;
import com.stemmeter.stem_meter.Sensors.Sensor;
import com.stemmeter.stem_meter.Sensors.TEMP_SI7021;
import com.stemmeter.stem_meter.Sensors.Temp_MCP9808;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ConnectFragment.ConnectFragInterface,
        SensorsFragment.SensorFragInterface,
        GraphFragment.GraphFragInterface,
        DisplayFragment.DisplayFragInterface,
        GraphSettingsFragment.GraphSettingsFragInterface {

    private String TAG = "MainActivity";
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    // Monroe's Base Unit Address
   // public final static String DEVICE_MAC_STR = "CC:78:AB:AC:B3:FE";

    // Josh's Base Unit Address
  //  public final static String DEVICE_MAC_STR = "CC:78:AB:19:9A:21";

    //public final static UUID BOARD_UUID = UUID.fromString("0000ABAE-0000-1000-8000-00805F9B34FB");
    public final static UUID SM_SERVICE_UUID =      UUID.fromString("F000ABAE-0451-4000-B000-000000000000");
    public final static UUID SENSOR_1_DATA_UUID =   UUID.fromString("F000BEAA-0451-4000-B000-000000000000");
    public final static UUID SENSOR_2_DATA_UUID =   UUID.fromString("F000BEAB-0451-4000-B000-000000000000");
    public final static UUID SENSOR_3_DATA_UUID =   UUID.fromString("F000BEAC-0451-4000-B000-000000000000");
    public final static UUID SENSOR_4_DATA_UUID =   UUID.fromString("F000BEAD-0451-4000-B000-000000000000");
    public final static UUID SENSOR_CONFIG_UUID =   UUID.fromString("F000BEAF-0451-4000-B000-000000000000");
    public final static UUID BATTERY_INFO_UUID =    UUID.fromString("F000BEBC-0451-4000-B000-000000000000");
    public final static UUID TIME_CONFIG_UUID =     UUID.fromString("F000BEBD-0451-4000-B000-000000000000");
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

    private BaseUnitBattery baseUnitBattery;

    private ArrayList<SensorConfig> sensorConfigList;

    private ArrayList<LineData> savedDataList;
    private ArrayList<String> savedNameList;

    private GraphConfig graphConfig;

    private Menu mainMenu;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

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

        sensorConfigList = new ArrayList<>();
        sensorConfigList.add(new SensorConfig(1));
        sensorConfigList.add(new SensorConfig(2));
        sensorConfigList.add(new SensorConfig(3));
        sensorConfigList.add(new SensorConfig(4));

        baseUnitBattery = new BaseUnitBattery();

        savedDataList = new ArrayList<LineData>();
        savedNameList = new ArrayList<String>();

        graphConfig = new GraphConfig();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        //noinspection RestrictedApi
        navigationView.setNavigationItemSelectedListener(this);

        gattDescriptors = new LinkedList<>();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This device does not support BLE", Toast.LENGTH_SHORT).show();
            finish();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can scan for BLE devices.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(23)
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        ConnectFragment connectFragment = new ConnectFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, connectFragment, CONNECT_FRAG_TAG).commit();

        printConnectionStat("Disconnected");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                    ConnectFragment connectFragment = new ConnectFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container, connectFragment, CONNECT_FRAG_TAG).commit();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }


    public void BoardConnect(BluetoothDevice device) {
        printConnectionStat("Connecting to board...");
        boardDevice = device;
        mainMenu.findItem(R.id.connection_icon).setIcon(R.drawable.ble_connecting);
        mBluetoothGatt = boardDevice.connectGatt(getApplicationContext(), true, mGattCallback);
    }

    public void BLEScan() {
        mBluetoothManager = (BluetoothManager) getSystemService(getApplicationContext().BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) { // if bluetooth is not enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            printConnectionStat("Disconnected");
        } else if (mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
            printConnectionStat("Scanning...");
            scanLeDevice(true);
        }
        else {
            mBluetoothGatt.disconnect();
            mConnectionState = STATE_DISCONNECTED;
            printConnectionStat("Scanning...");
            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        Log.i(TAG, "Scanning...");
        // Scan for 10 seconds only
        final long SCAN_PERIOD = 10000;
        mHandler = new Handler();
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if(mConnectionState == BluetoothProfile.STATE_DISCONNECTED) {
                        printConnectionStat("Disconnected");
                    }
                    Log.i(TAG, "Stopped Scanning");
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
                    if(device != null && device.getName() != null) {
                        if (device.getName().equals("STEM Meter")) {
                            ConnectFragment connectFragment = (ConnectFragment)
                                    getSupportFragmentManager().findFragmentByTag(CONNECT_FRAG_TAG);
                            if (connectFragment != null) {
                                BLEDevice bleDevice = new BLEDevice(device,Integer.toString(rssi));
                                connectFragment.addScanListItem(bleDevice);
                            }
                        }
                    }
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

                        // change the connection status icon to disconnected
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainMenu.findItem(R.id.connection_icon).setIcon(R.drawable.disconnected_icon);
                            }
                        });
                    }
                }

                @Override
                // Services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.i(TAG, "Services Discovered");
                        printConnectionStat("Services Discovered");
                        BoardService = mBluetoothGatt.getService(SM_SERVICE_UUID);
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
                        BoardTimeConfigChar = BoardService.getCharacteristic(TIME_CONFIG_UUID);
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

                        // change the connection status icon to connected
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainMenu.findItem(R.id.connection_icon).setIcon(R.drawable.ble_connected);
                            }
                        });

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
                        else if (characteristic == BoardBatteryInfoChar) {
                            String charString = characteristic.getStringValue(0);
                            Log.i(TAG,"Bat Char: " + charString);
                            baseUnitBattery.updateBatteryValues(charString);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Change where the battery info is displayed
                                    Toast.makeText(getApplicationContext(), baseUnitBattery.getBatStr(), Toast.LENGTH_LONG).show();
                                }
                            });
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

    // method to update sensor config objects using byte array data read from base unit
    public void updateSensorConfigData(byte configData[]) {
        // Byte 0 = S1 Freq | Byte 1 = S1 SD Log
        // Byte 2 = S2 Freq | Byte 3 = S2 SD Log
        // Byte 4 = S3 Freq | Byte 5 = S3 SD Log
        // Byte 6 = S4 Freq | Byte 7 = S4 SD Log
        Log.i(TAG,"Updating sensor config objects");

        sensorConfigList.get(0).setFreq((int) configData[0]);
        sensorConfigList.get(0).setSDLogging(configData[1] == 1);

        sensorConfigList.get(1).setFreq((int) configData[2]);
        sensorConfigList.get(1).setSDLogging(configData[3] == 1);

        sensorConfigList.get(2).setFreq((int) configData[4]);
        sensorConfigList.get(2).setSDLogging(configData[5] == 1);

        sensorConfigList.get(3).setFreq((int) configData[6]);
        sensorConfigList.get(3).setSDLogging(configData[7] == 1);
    }

    @Override
    public boolean updateBaseUnitTime() {
        Calendar calendar = new GregorianCalendar();
        byte month = (byte)calendar.get(Calendar.MONTH);
        byte day = (byte)calendar.get(Calendar.DAY_OF_MONTH);
        byte year = (byte)(calendar.get(Calendar.YEAR)-2000);
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
        timeData[2] = (byte)(month+1);
        timeData[3] = year;
        timeData[4] = hour;
        timeData[5] = minutes;
        timeData[6] = seconds;

        Log.i(TAG,"Setting Base Unit Time...");
        return writeCharacteristic(BoardTimeConfigChar, timeData);
    }

    @Override
    public void readSensorConfigData() {
        // Make sure BLE is connected before trying to read
        if (mBluetoothAdapter != null && mBluetoothGatt != null) {
            Log.i(TAG, "Reading Sensor Config");
            // Read the current config values from base unit
            // Once read callback will update config objects
            mBluetoothGatt.readCharacteristic(BoardSensorConfigChar);
        }
    }

    void handleSensor1Data(final byte sensor1Data[]) {
//      Log.i(TAG, "HANDLE S1");
        if (sensor1Data[0] == SensorConst.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            // sensor data type is held in first byte of sensor data
            switch (sensor1Data[0]) {
                case SensorConst.ACCEL_MPU6050:
                    sensor1 = new Accel_MPU6050(sensor1Data, 1);
                    break;
                case SensorConst.TEMP_MCP9808:
                    sensor1 = new Temp_MCP9808(sensor1Data, 1);
                    break;
                case SensorConst.GYRO_MPU6050:
                    sensor1 = new Gyro_MPU6050(sensor1Data, 1);
                    break;
                case SensorConst.LIGHT_OPT3002:
                    sensor1 = new LIGHT_OPT3002(sensor1Data, 1);
                    break;
                case SensorConst.MAG_MAG3110:
                    sensor1 = new MAG_MAG3110(sensor1Data, 1);
                    break;
                case SensorConst.PRESSURE_MPL3115A2:
                    sensor1 = new PRESSURE_MPL3115A2(sensor1Data, 1);
                    break;
                case SensorConst.TEMP_SI7021:
                    sensor1 = new TEMP_SI7021(sensor1Data, 1);
                    break;
                default:
                    Log.i(TAG,"Invalid sensor type detected");
                    return;
            }
        }
        // perform calculations on data
        sensor1.calcSensorData();
        // post the data to the screen
        postSensorData(sensor1);

    }

    void handleSensor2Data(final byte sensor2Data[]) {
    //  Log.i(TAG, "HANDLE S2");
        if (sensor2Data[0] == SensorConst.INVALID_SENSOR) {
            return;
        } else {
            switch (sensor2Data[0]) {
                case SensorConst.ACCEL_MPU6050:
                    sensor2 = new Accel_MPU6050(sensor2Data, 2);
                    break;
                case SensorConst.TEMP_MCP9808:
                    sensor2 = new Temp_MCP9808(sensor2Data, 2);
                    break;
                case SensorConst.GYRO_MPU6050:
                    sensor2 = new Gyro_MPU6050(sensor2Data, 2);
                    break;
                case SensorConst.LIGHT_OPT3002:
                    sensor2 = new LIGHT_OPT3002(sensor2Data, 2);
                    break;
                case SensorConst.MAG_MAG3110:
                    sensor2 = new MAG_MAG3110(sensor2Data, 2);
                    break;
                case SensorConst.PRESSURE_MPL3115A2:
                    sensor2 = new PRESSURE_MPL3115A2(sensor2Data, 2);
                    break;
                case SensorConst.TEMP_SI7021:
                    sensor2 = new TEMP_SI7021(sensor2Data, 2);
                    break;
                default:
                    Log.i(TAG,"Invalid sensor type detected");
                    return;
            }
        }
        // perform calculations on data
        sensor2.calcSensorData();
        // post the data to the screen
        postSensorData(sensor2);
    }

    void handleSensor3Data(final byte sensor3Data[]) {

//      Log.i(TAG, "HANDLE S3");
        if (sensor3Data[0] == SensorConst.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            // sensor data type is held in first byte of sensor data
            switch (sensor3Data[0]) {
                case SensorConst.ACCEL_MPU6050:
                    sensor3 = new Accel_MPU6050(sensor3Data, 3);
                    break;
                case SensorConst.TEMP_MCP9808:
                    sensor3 = new Temp_MCP9808(sensor3Data, 3);
                    break;
                case SensorConst.GYRO_MPU6050:
                    sensor3 = new Gyro_MPU6050(sensor3Data, 3);
                    break;
                case SensorConst.LIGHT_OPT3002:
                    sensor3 = new LIGHT_OPT3002(sensor3Data, 3);
                    break;
                case SensorConst.MAG_MAG3110:
                    sensor3 = new MAG_MAG3110(sensor3Data, 3);
                    break;
                case SensorConst.PRESSURE_MPL3115A2:
                    sensor3 = new PRESSURE_MPL3115A2(sensor3Data, 3);
                    break;
                case SensorConst.TEMP_SI7021:
                    sensor3 = new TEMP_SI7021(sensor3Data, 3);
                    break;
                default:
                    Log.i(TAG,"Invalid sensor type detected");
                    return;
            }
        }
        // perform calculations on data
        sensor3.calcSensorData();
        // post the data to the screen
        postSensorData(sensor3);
    }

    void handleSensor4Data(final byte sensor4Data[]) {

       Log.i(TAG, "HANDLE S4");
        if (sensor4Data[0] == SensorConst.INVALID_SENSOR) {
            return;
        } else {
            // check to see which sensor is connected
            // sensor data type is held in first byte of sensor data
            switch (sensor4Data[0]) {
                case SensorConst.ACCEL_MPU6050:
                    sensor4 = new Accel_MPU6050(sensor4Data, 4);
                    break;
                case SensorConst.TEMP_MCP9808:
                    sensor4 = new Temp_MCP9808(sensor4Data, 4);
                    break;
                case SensorConst.GYRO_MPU6050:
                    sensor4 = new Gyro_MPU6050(sensor4Data, 4);
                    break;
                case SensorConst.LIGHT_OPT3002:
                    sensor4 = new LIGHT_OPT3002(sensor4Data, 4);
                    break;
                case SensorConst.MAG_MAG3110:
                    sensor4 = new MAG_MAG3110(sensor4Data, 4);
                    break;
                case SensorConst.PRESSURE_MPL3115A2:
                    sensor4 = new PRESSURE_MPL3115A2(sensor4Data, 4);
                    break;
                case SensorConst.TEMP_SI7021:
                    sensor4 = new TEMP_SI7021(sensor4Data, 4);
                    break;
                default:
                    Log.i(TAG,"Invalid sensor type detected");
                    return;
            }
        }
        // perform calculations on data
        sensor4.calcSensorData();
        // post the data to the screen
        postSensorData(sensor4);
    }

    public void postSensorData(Sensor sensor) {
        SensorsFragment sensorsFragment = (SensorsFragment)
                getSupportFragmentManager().findFragmentByTag(SENSOR_FRAG_TAG);
       // Log.i(TAG,"Check if Sensors are up");
        // if the sensor fragment is showing print the data there
        if (sensorsFragment != null && sensorsFragment.isVisible()) {
            sensorsFragment.printSensorData(sensor.getSensorNumber(), sensor.toString());
        }
        // Determines whether to add sensor data to graph
        else if(sensor.getSensorNumber() == (graphConfig.getSelectedSensor() + 1)) {
           // Log.i(TAG,"Check if graph Fragment is up");
            GraphFragment graphFragment = (GraphFragment)
                    getSupportFragmentManager().findFragmentByTag(GRAPH_FRAG_TAG);
            if (graphFragment != null && graphFragment.isVisible()) {
                // TODO post the ArrayList of graph data to the graph fragment
                // get ArrayList of graph floats
                Log.i(TAG,"Adding data to graph");
                ArrayList<Float> graphDataList = sensor.getGraphData();
                graphFragment.addGraphEntry(graphDataList, sensor.getNumberDataPoints());
            }
        }
        else {
            // Log.i(TAG,"Graph and Sensors are not up. Selected Sensor: " + graphConfig.getSelectedSensor() );
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
        mainMenu = menu;
        mainMenu.findItem(R.id.connection_icon).setIcon(R.drawable.disconnected_icon);
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
            if (mBluetoothAdapter == null || mBluetoothGatt == null ||  mConnectionState == STATE_DISCONNECTED) {
                Toast.makeText(this,"Not Connected", Toast.LENGTH_SHORT).show();
            }
            else if(BoardBatteryInfoChar != null) {
                // Read the battery info
                mBluetoothGatt.readCharacteristic(BoardBatteryInfoChar);
            }
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
        // Set the appropriate config to the new settings
        sensorConfigList.get(config.getSensorNumber()-1).setFreq(config.getFreq());
        sensorConfigList.get(config.getSensorNumber()-1).setSDLogging(config.isSDLogging());

        // Byte 0 = S1 Freq | Byte 1 = S1 SD Log
        // Byte 2 = S2 Freq | Byte 3 = S2 SD Log
        // Byte 4 = S3 Freq | Byte 5 = S3 SD Log
        // Byte 6 = S4 Freq | Byte 7 = S4 SD Log

        byte[] configData = new byte[8];

        configData[0] = (byte)sensorConfigList.get(0).getFreq();
        configData[1] = (byte)((sensorConfigList.get(0).isSDLogging()) ? 1 : 0);

        configData[2] = (byte)sensorConfigList.get(1).getFreq();
        configData[3] = (byte)((sensorConfigList.get(1).isSDLogging()) ? 1 : 0);

        configData[4] = (byte)sensorConfigList.get(2).getFreq();
        configData[5] = (byte)((sensorConfigList.get(2).isSDLogging()) ? 1 : 0);

        configData[6] = (byte)sensorConfigList.get(3).getFreq();
        configData[7] = (byte)((sensorConfigList.get(3).isSDLogging()) ? 1 : 0);

        return writeCharacteristic(BoardSensorConfigChar, configData);
    }

    @Override
    public SensorConfig getSensorConfig(int sensorNumber) {
        return sensorConfigList.get(sensorNumber-1);
    }

    @Override
    public ArrayList<LineData> getSavedList(){

        return savedDataList;
    }

    @Override
    public ArrayList<String> getSavedNameList(){

        return savedNameList;
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

    @Override
    public Sensor getSensor(int sensorNumber) {
        Sensor returnSesnor = null;

        switch(sensorNumber) {
            case SensorConst.SENSOR_1:
                returnSesnor = sensor1;
                break;
            case SensorConst.SENSOR_2:
                returnSesnor = sensor2;
                break;
            case SensorConst.SENSOR_3:
                returnSesnor = sensor3;
                break;
            case SensorConst.SENSOR_4:
                returnSesnor = sensor4;
                break;

        }
        return returnSesnor;
    }

}