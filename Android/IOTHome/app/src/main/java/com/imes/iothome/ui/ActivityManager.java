package com.imes.iothome.ui;

import java.util.ArrayList;

import android.app.Activity;

public class ActivityManager
{

	private static ActivityManager mActivityMananger = null;
	private ArrayList<Activity> mActivityList = null;

	private ActivityManager()
	{
		mActivityList = new ArrayList<Activity>();
	}

	public static ActivityManager getInstance()
	{

		if (ActivityManager.mActivityMananger == null)
		{
			mActivityMananger = new ActivityManager();
		}
		return mActivityMananger;
	}

	/**
	 * 액티비티 리스트 getter.
	 * 
	 * @return activityList
	 */
	public ArrayList<Activity> getActivityList()
	{
		return mActivityList;
	}

	/**
	 * 액티비티 리스트에 추가.
	 * 
	 * @param activity
	 */
	public void addActivity(Activity activity)
	{
		mActivityList.add(activity);
	}

	/**
	 * 액티비티 리스트에서 삭제.
	 * 
	 * @param activity
	 * @return boolean
	 */
	public boolean removeActivity(Activity activity)
	{
		return mActivityList.remove(activity);
	}

	/**
	 * 모든 액티비티 종료.
	 */
	public void finishAllActivity()
	{
		for (Activity activity : mActivityList)
		{
			activity.finish();
		}
	}

}
