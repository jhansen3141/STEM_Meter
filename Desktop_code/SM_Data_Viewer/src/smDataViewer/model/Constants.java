package smDataViewer.model;

public class Constants {
	// Markers
	public static final byte LOG_MARKER_B0 = (byte) 0x55;
	public static final byte LOG_MARKER_B1 = (byte) 0xAA;
	public static final byte LOG_MARKER_B2 = (byte) 0xA5;

	public static final byte TIME_MARKER_B0 = (byte) 0xAA;
	public static final byte TIME_MARKER_B1 = (byte) 0xBB;
	public static final byte TIME_MARKER_B2 = (byte) 0xCC;
	public static final byte TIME_MARKER_END = (byte) 0xDD;

	// Sensor Numbers
    public static final int SENSOR_1 = 1;
    public static final int SENSOR_2 = 2;
    public static final int SENSOR_3 = 3;
    public static final int SENSOR_4 = 4;

    // Sensor IDs
    public static final int INVALID_SENSOR = 0;
    public static final int TEMP_MCP9808 = 1;
    public static final int IMU_MPU6050 = 2;
	    public static final int IMU_ACCEL_X = 0;
	    public static final int IMU_ACCEL_Y = 1;
	    public static final int IMU_ACCEL_Z = 2;
	    public static final int IMU_GYRO_X = 3;
	    public static final int IMU_GYRO_Y = 4;
	    public static final int IMU_GYRO_Z = 5;

    // Update Rates
    public static final int RATE_OFF = 0;
    public static final int RATE_TEN_HZ = 1;
    public static final int RATE_FIVE_HZ = 2;
    public static final int RATE_ONE_HZ = 3;
    public static final int RATE_ONE_MIN = 4;
    public static final int RATE_TEN_MIN = 5;
    public static final int RATE_THRITY_MIN = 6;
    public static final int RATE_ONE_HOUR = 7;
}
