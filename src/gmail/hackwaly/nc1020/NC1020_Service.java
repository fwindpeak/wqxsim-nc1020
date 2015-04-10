package gmail.hackwaly.nc1020;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;

public class NC1020_Service extends Service {

	public static final int RESULT_QUIT = 0;
	public static final int RESULT_FRAME = 1;

	private static final int FRAME_RATE = 50;
	private static final int FRAME_INTERVAL = 1000 / FRAME_RATE;
	private ResultReceiver activityReceiver;
	private Handler frameHandler;
	private boolean mStopped;
	private boolean mSpeedUp;
	private boolean mBackground;
	private final Runnable frameRunnable = new Runnable(){

		@Override
		public void run() {
			NC1020_JNI.RunTimeSlice(FRAME_INTERVAL, mSpeedUp);
			frameHandler.postDelayed(frameRunnable, FRAME_INTERVAL);
			if (!mBackground && activityReceiver != null) {
				activityReceiver.send(RESULT_FRAME, null);
			}
		}
		
	};
	
	@Override
	public void onCreate(){
		mStopped = true;
		frameHandler = new Handler();
		mSpeedUp = false;
		mBackground = false;
	}
	
	@Override
	public void onDestroy(){
		stopEmulation();
		if (activityReceiver != null) {
			activityReceiver.send(RESULT_QUIT, null);
			activityReceiver = null;
		}
	}
	
	private void startEmulation(){
		if (mStopped) {
			frameHandler.post(frameRunnable);
			mStopped = false;
		}
	}
	
	private void stopEmulation(){
		if (!mStopped) {
			frameHandler.removeCallbacks(frameRunnable);
			mStopped = true;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action == "tellService") {
			ResultReceiver receiver = intent.getParcelableExtra("value");
			if (receiver != null) {
				activityReceiver = receiver;
				startEmulation();
			} else {
				stopSelf();
			}
		} else if (action == "tellBackground") {
			mBackground = intent.getBooleanExtra("value", false);
		} else if (action == "tellSpeedUp") {
			mSpeedUp = intent.getBooleanExtra("value", false);
		}
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
