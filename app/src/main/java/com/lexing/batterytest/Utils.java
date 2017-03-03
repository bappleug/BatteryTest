/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lexing.batterytest;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.BatteryManager;

import com.lexing.batterytest.batteryhelper.AutoBootAppInfo;
import com.lexing.batterytest.batteryhelper.BatterySipper;
import com.lexing.batterytest.batteryhelper.RunningAppInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Contains utility functions for formatting elapsed time and consumed bytes
 */
public class Utils {
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int SECONDS_PER_HOUR = 60 * 60;
	private static final int SECONDS_PER_DAY = 24 * 60 * 60;

	/**
	 * Returns elapsed time for the given millis, in the following format: 2d 5h 40m 29s
	 * 
	 * @param context
	 *            the application context
	 * @param millis
	 *            the elapsed time in milli seconds
	 * @return the formatted elapsed time
	 */
	public static String formatElapsedTime(Context context, double millis) {
		StringBuilder sb = new StringBuilder();
		int seconds = (int) Math.floor(millis / 1000);

		int days = 0, hours = 0, minutes = 0;
		if (seconds > SECONDS_PER_DAY) {
			days = seconds / SECONDS_PER_DAY;
			seconds -= days * SECONDS_PER_DAY;
		}
		if (seconds > SECONDS_PER_HOUR) {
			hours = seconds / SECONDS_PER_HOUR;
			seconds -= hours * SECONDS_PER_HOUR;
		}
		if (seconds > SECONDS_PER_MINUTE) {
			minutes = seconds / SECONDS_PER_MINUTE;
			seconds -= minutes * SECONDS_PER_MINUTE;
		}
		if (days > 0) {
			sb.append(context.getString(R.string.battery_history_days, days, hours, minutes, seconds));
		} else if (hours > 0) {
			sb.append(context.getString(R.string.battery_history_hours, hours, minutes, seconds));
		} else if (minutes > 0) {
			sb.append(context.getString(R.string.battery_history_minutes, minutes, seconds));
		} else {
			sb.append(context.getString(R.string.battery_history_seconds, seconds));
		}
		return sb.toString();
	}

	/**
	 * Formats data size in KB, MB, from the given bytes.
	 * 
	 * @param context
	 *            the application context
	 * @param bytes
	 *            data size in bytes
	 * @return the formatted size such as 4.52 MB or 245 KB or 332 bytes
	 */
	public static String formatBytes(Context context, double bytes) {
		// TODO: I18N
		if (bytes > 1000 * 1000) {
			return String.format("%.2f MB", ((int) (bytes / 1000)) / 1000f);
		} else if (bytes > 1024) {
			return String.format("%.2f KB", ((int) (bytes / 10)) / 100f);
		} else {
			return String.format("%ruid bytes", (int) bytes);
		}
	}

	public static String getBatteryPercentage(Intent batteryChangedIntent) {
		int level = batteryChangedIntent.getIntExtra("level", 0);
		int scale = batteryChangedIntent.getIntExtra("scale", 100);
		return String.valueOf(level * 100 / scale) + "%";
	}

	public static String getBatteryStatus(Resources res, Intent batteryChangedIntent) {
		final Intent intent = batteryChangedIntent;

		int plugType = intent.getIntExtra("plugged", 0);
		int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		String statusString;
		if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
			statusString = res.getString(R.string.battery_info_status_charging);
			if (plugType > 0) {
				statusString = statusString
						+ " "
						+ res.getString((plugType == BatteryManager.BATTERY_PLUGGED_AC) ? R.string.battery_info_status_charging_ac
								: R.string.battery_info_status_charging_usb);
			}
		} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
			statusString = res.getString(R.string.battery_info_status_discharging);
		} else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
			statusString = res.getString(R.string.battery_info_status_not_charging);
		} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
			statusString = res.getString(R.string.battery_info_status_full);
		} else {
			statusString = res.getString(R.string.battery_info_status_unknown);
		}

		return statusString;
	}

	public static void getQuickNameIcon(Context context, BatterySipper sipper) {
		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(sipper.getPkgName(), 0);
			sipper.setIcon(appInfo.loadIcon(pm));// pm.getApplicationIcon(appInfo);
			sipper.setName(appInfo.loadLabel(pm).toString());// pm.getApplicationLabel(appInfo).toString();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void getQuickNameIcon(Context context, RunningAppInfo runningAppInfo) {
		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(runningAppInfo.pkgName, 0);
			runningAppInfo.icon = appInfo.loadIcon(pm);// pm.getApplicationIcon(appInfo);
			runningAppInfo.name = appInfo.loadLabel(pm).toString();// pm.getApplicationLabel(appInfo).toString();
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use reflect to get Package Usage Statistics data.<br>
	 */
	public static void getAutoBootsNum(Context context, List<AutoBootAppInfo> autoBootAppInfos) {
		UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
		long endTime = System.currentTimeMillis();
		long beginTime = endTime - 1000 * 60 * 60 * 24;
		List<UsageStats> usageStatses = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, beginTime, endTime);
		try {
			if(usageStatses != null && usageStatses.size() >= 3){
				Class<UsageStats> usageStatsClazz = UsageStats.class;
				for(UsageStats usageStats : usageStatses){
					String packageName = usageStats.getPackageName();
					for(AutoBootAppInfo autoBootAppInfo : autoBootAppInfos){
						if(autoBootAppInfo.desc.equals(packageName)){
							int launchCount = usageStatsClazz
									.getDeclaredField("mLaunchCount").getInt(usageStats);
							long usageTime = usageStats.getTotalTimeInForeground();
							autoBootAppInfo.boots = "自动重启：" + launchCount + "次";
							autoBootAppInfo.runningTime = usageTime / 1000 + "s";
						}
					}
				}
			}

			Class<?> cServiceManager = Class
					.forName("android.os.ServiceManager");
			Method mGetService = cServiceManager.getMethod("getService",
					java.lang.String.class);
			Object oRemoteService = mGetService.invoke(null, "usagestats");

			// IUsageStats oIUsageStats =
			// IUsageStats.Stub.asInterface(oRemoteService)
			Class<?> cStub = Class
					.forName("com.android.internal.app.IUsageStats$Stub");
			Method mUsageStatsService = cStub.getMethod("asInterface",
					android.os.IBinder.class);
			Object oIUsageStats = mUsageStatsService.invoke(null,
					oRemoteService);

			// PkgUsageStats[] oPkgUsageStatsArray =
			// mUsageStatsService.getAllPkgUsageStats();
			Class<?> cIUsageStatus = Class
					.forName("com.android.internal.app.IUsageStats");
			Method mGetAllPkgUsageStats = cIUsageStatus.getMethod(
					"getAllPkgUsageStats", (Class[]) null);
			Object[] oPkgUsageStatsArray = (Object[]) mGetAllPkgUsageStats
					.invoke(oIUsageStats, (Object[]) null);

			Class<?> cPkgUsageStats = Class
					.forName("com.android.internal.os.PkgUsageStats");

			for (Object pkgUsageStats : oPkgUsageStatsArray) {
				// get pkgUsageStats.packageName, pkgUsageStats.launchCount,
				// pkgUsageStats.usageTime
				String packageName = (String) cPkgUsageStats.getDeclaredField(
						"packageName").get(pkgUsageStats);
				for(AutoBootAppInfo autoBootAppInfo : autoBootAppInfos){
					if(autoBootAppInfo.desc.equals(packageName)){
						int launchCount = cPkgUsageStats
								.getDeclaredField("launchCount").getInt(pkgUsageStats);
						long usageTime = cPkgUsageStats.getDeclaredField("usageTime")
								.getLong(pkgUsageStats);
						autoBootAppInfo.boots = "自动重启：" + launchCount + "次";
						autoBootAppInfo.runningTime = usageTime / 1000 + "s";
					}
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}
