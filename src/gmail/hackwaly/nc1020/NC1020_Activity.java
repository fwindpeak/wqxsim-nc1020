package gmail.hackwaly.nc1020;

import gmail.hackwaly.nc1020.NC1020_KeypadView.OnKeyListener;

import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;

public class NC1020_Activity extends Activity implements Callback,
		OnKeyListener {
	private byte[] lcdBuffer;
	private byte[] lcdBufferEx;
	private Bitmap lcdBitmap;
	private Bitmap numBitmap;
	private Matrix lcdMatrix;
	private SurfaceView lcdSurfaceView;
	private SurfaceHolder lcdSurfaceHolder;
	private NC1020_KeypadView gmudKeypad;
	private SharedPreferences prefs;

	private class NC1020_ResultReceiver extends ResultReceiver {
		public NC1020_ResultReceiver(Handler handler) {
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			switch (resultCode) {
			case NC1020_Service.RESULT_QUIT:
				NC1020_JNI.Save();
				finish();
				break;
			case NC1020_Service.RESULT_FRAME:
				updateLcd();
				break;
			}
		}
	}

	private NC1020_ResultReceiver frameReceiver;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		gmudKeypad = (NC1020_KeypadView) findViewById(R.id.gmud_keypad);
		gmudKeypad.setOnKeyListener(this);

		lcdSurfaceView = (SurfaceView) findViewById(R.id.lcd);

		int w = this.getWindowManager().getDefaultDisplay().getWidth();

		LayoutParams params = new LayoutParams(w, w / 2+20);

		params.leftMargin = 0;
		params.topMargin = 0;

		lcdSurfaceView.setLayoutParams(params);

		lcdSurfaceHolder = lcdSurfaceView.getHolder();
		lcdBuffer = new byte[1600];
		lcdBufferEx = new byte[1600 * 8];
		lcdBitmap = Bitmap.createBitmap(160, 80, Bitmap.Config.ALPHA_8);
		numBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.num);
		lcdMatrix = new Matrix();

		lcdSurfaceHolder.addCallback(this);
		frameReceiver = new NC1020_ResultReceiver(null);
		NC1020_JNI.Initialize(Environment.getExternalStorageDirectory()
				.getPath() + "/nc1020");
		NC1020_JNI.Load();
		tellService(true);
	}

	@Override
	public void onResume() {
		tellBackground(false);
		super.onResume();
	}

	@Override
	public void onPause() {
		tellBackground(true);
		super.onPause();
	}

	private void tellService(boolean startOrStop) {
		Intent intent = new Intent(this, NC1020_Service.class);
		intent.setAction("tellService");
		intent.putExtra("value", startOrStop ? frameReceiver : null);
		startService(intent);
	}

	private void tellSpeedUp(boolean speedUp) {
		Intent intent = new Intent(this, NC1020_Service.class);
		intent.setAction("tellSpeedUp");
		intent.putExtra("value", speedUp);
		startService(intent);
	}

	private void tellBackground(boolean background) {
		Intent intent = new Intent(this, NC1020_Service.class);
		intent.setAction("tellBackground");
		intent.putExtra("value", background);
		startService(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			NC1020_JNI.SetKey(0x3B, true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			NC1020_JNI.SetKey(0x3B, false);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateLcd() {
		if (!NC1020_JNI.CopyLcdBuffer(lcdBuffer)) {
			return;
		}
		Canvas lcdCanvas = lcdSurfaceHolder.lockCanvas();
		for (int y = 0; y < 80; y++) {
			for (int j = 0; j < 20; j++) {
				byte p = lcdBuffer[20 * y + j];
				for (int k = 0; k < 8; k++) {
					lcdBufferEx[y * 160 + j * 8 + k] = (byte) ((p & (1 << (7 - k))) != 0 ? 0xFF
							: 0x00);
				}
			}
		}
		for (int y = 0; y < 80; y++) {
			lcdBufferEx[y * 160] = 0;
		}
		lcdBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(lcdBufferEx));
		// lcdCanvas.drawColor(0xFF72B056);
		lcdCanvas.drawColor(0xFF80B080);
		// lcdCanvas.drawBitmap(lcdBitmap, lcdMatrix, null);

		// lcdCanvas.drawBitmap(lcdBitmap,null, new Rect(0, 0, 159, 79), null);
		
		int w = lcdCanvas.getWidth();
		int h = lcdCanvas.getHeight();
		
		lcdCanvas.drawBitmap(lcdBitmap, null,
				new Rect(0, 0, w, h-20),
				null);
//		lcdCanvas.drawBitmap(numBitmap, null,new Rect(0,h-20,w,h), null);
		
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		Paint paint2 = new Paint();
		paint2.setTextSize(20);
		paint2.setColor(Color.BLACK);
		
		lcdCanvas.drawRect(0, h-20, w, h, paint);
		for(int i=1;i<=9;i++){
			lcdCanvas.drawText(i+"", (w/10)*i+(w/10/4), h, paint2);
		}

		lcdSurfaceHolder.unlockCanvasAndPost(lcdCanvas);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		menu.findItem(R.id.action_speed_up).setChecked(
				prefs.getBoolean("SpeedUp", false));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_quit:
			tellService(false);
			finish();
			return true;
		case R.id.action_restart:
			NC1020_JNI.Reset();
			return true;
		case R.id.action_speed_up:
			if (item.isChecked()) {
				item.setChecked(false);
			} else {
				item.setChecked(true);
			}
			tellSpeedUp(item.isChecked());
			prefs.edit().putBoolean("SpeedUp", item.isChecked()).commit();
			return true;
		case R.id.action_load:
			NC1020_JNI.Load();
			return true;
		case R.id.action_save:
			NC1020_JNI.Save();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		lcdMatrix.setScale(3, 3);

		Canvas lcdCanvas = lcdSurfaceHolder.lockCanvas();
		lcdCanvas.drawColor(0xFF72B056);
		lcdSurfaceHolder.unlockCanvasAndPost(lcdCanvas);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}

	@Override
	public void onKeyDown(int keyId) {
		NC1020_JNI.SetKey(keyId, true);
	}

	@Override
	public void onKeyUp(int keyId) {
		NC1020_JNI.SetKey(keyId, false);
	}

}
