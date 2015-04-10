package gmail.hackwaly.nc1020;

public class NC1020_JNI {
	public static native void Initialize(String path);
	public static native void Reset();
	public static native void Load();
	public static native void Save();
	public static native void SetKey(int keyId, boolean downOrUp);
	public static native void RunTimeSlice(int timeSlice, boolean speedUp);
	public static native boolean CopyLcdBuffer(byte[] buffer);
	static {
		System.loadLibrary("NC1020");
	}
}
