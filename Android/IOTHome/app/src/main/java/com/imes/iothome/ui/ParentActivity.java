package com.imes.iothome.ui;

import android.app.Activity;
import android.os.Bundle;

public class ParentActivity extends Activity
{

	private ActivityManager am = ActivityManager.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		am.addActivity(this);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		am.removeActivity(this);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		am.finishAllActivity();
	}

}
