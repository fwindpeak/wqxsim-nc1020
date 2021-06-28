package gmail.hackwaly.nc1020;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class NC1020_KeypadView extends LinearLayout implements
		android.view.View.OnTouchListener {
	
	public interface OnKeyListener {
		void onKeyDown(int keyId);

		void onKeyUp(int keyId);
	}

	private OnKeyListener keyListener;

	private class KeyItem {
		public int keyId;
		public String keyLabel;
		public int keyCode;

		public KeyItem(int keyId, String keyLabel) {
			this(keyId, keyLabel, -1);
		}

		public KeyItem(int keyId, String keyValue, int keyCode) {
			this.keyId = keyId;
			this.keyLabel = keyValue;
			this.keyCode = keyCode;
		}
	};

	private KeyItem[][] items = {
			{
				new KeyItem(0x0B, "英汉"),
				new KeyItem(0x0C, "名片"),
				new KeyItem(0x0D, "计算"),
				new KeyItem(0x0A, "行程"),
				new KeyItem(0x09, "测验"),
				new KeyItem(0x08, "其他"),
				new KeyItem(0x0E, "网络"),
				new KeyItem(-1, null),
				new KeyItem(-1, null),
				new KeyItem(0x0F, "电源"),
			},
			{
				new KeyItem(-1, null),
				new KeyItem(-1, null),
				new KeyItem(-1, null),
				new KeyItem(-1, null),
				new KeyItem(-1, null),
				new KeyItem(-1, null),
				new KeyItem(0x10, "F1"),
				new KeyItem(0x11, "F2"),
				new KeyItem(0x12, "F3"),
				new KeyItem(0x13, "F4"),
			},
			{
				new KeyItem(0x20, "Q"),
				new KeyItem(0x21, "W"),
				new KeyItem(0x22, "E"),
				new KeyItem(0x23, "R"),
				new KeyItem(0x24, "T 7"),
				new KeyItem(0x25, "Y 8"),
				new KeyItem(0x26, "U 9"),
				new KeyItem(0x27, "I"),
				new KeyItem(0x18, "O"),
				new KeyItem(0x1C, "P"),
			},
			{
				new KeyItem(0x28, "A"),
				new KeyItem(0x29, "S"),
				new KeyItem(0x2A, "D"),
				new KeyItem(0x2B, "F"),
				new KeyItem(0x2C, "G 4"),
				new KeyItem(0x2D, "H 5"),
				new KeyItem(0x2E, "J 6"),
				new KeyItem(0x2F, "K"),
				new KeyItem(0x19, "L"),
				new KeyItem(0x1D, "Enter"),
			},
			{
				new KeyItem(0x30, "Z"),
				new KeyItem(0x31, "X"),
				new KeyItem(0x32, "C"),
				new KeyItem(0x33, "V"),
				new KeyItem(0x34, "B 1"),
				new KeyItem(0x35, "N 2"),
				new KeyItem(0x36, "M 3"),
				new KeyItem(0x37, "PgUp"),
				new KeyItem(0x1A, "Up"),
				new KeyItem(0x1E, "PgDn"),
			},
			{
				new KeyItem(0x38, "Help"),
				new KeyItem(0x39, "Shift"),
				new KeyItem(0x3A, "Caps"),
				new KeyItem(0x3B, "Esc"),
				new KeyItem(0x3C, "0"),
				new KeyItem(0x3D, "."),
				new KeyItem(0x3E, "="),
				new KeyItem(0x3F, "Left"),
				new KeyItem(0x1B, "Down"),
				new KeyItem(0x1F, "Right"),
			},
	};

	public void setOnKeyListener(OnKeyListener listener) {
		keyListener = listener;
	}

	public NC1020_KeypadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		initKeyBoard();
	}

	public NC1020_KeypadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initKeyBoard();
	}

	public NC1020_KeypadView(Context context) {
		super(context);
		initKeyBoard();
	}

	private void initKeyBoard() {
		this.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		params.leftMargin = 0;
		params.rightMargin = 0;
		for (int j = 0; j < items.length; j++) {
			LinearLayout linear = new LinearLayout(getContext());
			for (int i = 0; i < items[j].length; i++) {
				KeyItem item = items[j][i];
				Button button = new Button(getContext());
				if (item.keyId == -1) {
					button.setVisibility(INVISIBLE);
				} else {
					button.setOnTouchListener(this);
					button.setFocusable(false);
					button.setPadding(0, 0, 0, 0);
					button.setTextSize(10);
					button.setTag(item);
					button.setText(item.keyLabel);
					button.setSingleLine();
				}
				button.setLayoutParams(params);
				linear.addView(button);
			}
			this.addView(linear);
		}
	}

	public boolean onKeyEvent(int keyCode, int action) {
		for (KeyItem[] items_ : items) {
			for (KeyItem item: items_) {
				if (item.keyCode == keyCode) {
					if (action == KeyEvent.ACTION_DOWN) {
						keyListener.onKeyDown(item.keyId);
					} else {
						keyListener.onKeyUp(item.keyId);
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		KeyItem item = (KeyItem) view.getTag();
		if (keyListener == null) {
			return false;
		}
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			keyListener.onKeyDown(item.keyId);
		} else if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL) {
			keyListener.onKeyUp(item.keyId);
		}
		return false;
	}
}
