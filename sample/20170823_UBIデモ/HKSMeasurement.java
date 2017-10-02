import com.ntt.tc.data.*;
import com.ntt.tc.data.sensor.*;

/**
 * HKS の9軸情報を1つのシリーズとして表現するクラス
 */
public class HKSMeasurement extends C8yData {
	public Value accel_x;
	public Value accel_y;
	public Value accel_z;
	public Value gyro_x;
	public Value gyro_y;
	public Value gyro_z;
	public Value mag_x;
	public Value mag_y;
	public Value mag_z;
	
	public HKSMeasurement(double ax, double ay, double az,
				double gx, double gy, double gz,
				double mx, double my, double mz) {
		accel_x = new Value(ax, "m/s2"); // HKS は mG (重力加速度ベース)
		accel_y = new Value(ay, "m/s2");
		accel_z = new Value(az, "m/s2");
		gyro_x = new Value(gx, "deg/s");
		gyro_y = new Value(gy, "deg/s");
		gyro_z = new Value(gz, "deg/s");
		mag_x = new Value(mx, "uT");
		mag_y = new Value(my, "uT");
		mag_z = new Value(mz, "uT");
	}
}
