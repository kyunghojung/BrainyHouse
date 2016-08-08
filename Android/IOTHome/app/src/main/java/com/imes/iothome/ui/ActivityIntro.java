package com.imes.iothome.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.imes.iothome.R;

public class ActivityIntro extends Activity
{
	public static final String TAG = ActivityIntro.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

	boolean isStarted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_intro);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(isStarted == false)
		{
			isStarted = true;
			LoadingStart();
        }
		else
		{
			isStarted = false;
			LoadingStop();
		}
	}

	@Override
	protected void onDestroy()
	{
        RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		super.onDestroy();
	}

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
    }

	private void LoadingStart()
	{
		Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if(checkBluetoothSetting() == true)
				{
					Intent intent = new Intent(getApplicationContext(), ActivityMainFrame.class);
					startActivity(intent);
					finish();
				}
				else
				{
					displayBTSettingPopup();
                }
            }
		};
		handler.sendEmptyMessageDelayed(0, 3000);
	}

	private void LoadingStop()
	{
		Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				if(checkBluetoothSetting() == true)
				{
					Intent intent = new Intent(getApplicationContext(), ActivityMainFrame.class);
					startActivity(intent);
					finish();
				}
				else
				{
					displayBTSettingPopup();
				}
			}
		};
		handler.sendEmptyMessageDelayed(0, 3000);
	}

	private boolean checkBluetoothSetting()
	{
        BluetoothAdapter bluetoothAdapter = null;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
        {
            return false;
        }

        if(bluetoothAdapter.isEnabled() == false)
        {
            return false;
        }

        return true;
	}

	private void displayBTSettingPopup() {
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(getApplicationContext(), ActivityMainFrame.class);
                    startActivity(intent);
                    finish();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }
}
