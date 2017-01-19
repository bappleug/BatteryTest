package com.lexing.batterytest.batteryhelper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray on 2017/1/17.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class BatteryStatsHelperWrapper extends Wrapper {

    private static final String className = "com.android.internal.os.BatteryStatsHelper";
    public static final int MIN_POWER_THRESHOLD = 5;

    private UserManager mUm;

    protected BatteryStatsHelperWrapper(Object wrappedObj) {
        super(wrappedObj);
    }

    @Override
    protected String className() {
        return className;
    }

    public static BatteryStatsHelperWrapper create(Context context, boolean collectBatteryBroadcast) {
        try {
            Object powerProfile = Class.forName(className)
                    .getConstructor(Context.class, Boolean.TYPE)
                    .newInstance(context, collectBatteryBroadcast);
            BatteryStatsHelperWrapper helper = new BatteryStatsHelperWrapper(powerProfile);
            helper.mUm = (UserManager) context.getSystemService(Context.USER_SERVICE);
            return helper;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public void create(Bundle icicle) {
        invoke("create", new Class[]{Bundle.class}, new Object[]{icicle});
    }

    public void clearStats() {
        invoke("clearStats");
    }

//    public static void dropFile(Context context, String fname){
//        invokeStatic(className, "dropFile", new Class[]{Context.class, String.class}, new Object[]{context, fname});
//    }

    public void storeState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invoke("storeState");
        } else {
            invoke("destroy");
        }
    }

    public void refreshStats(int statsType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            invoke("refreshStats", new Class[]{Integer.TYPE, List.class}, new Object[]{statsType, mUm.getUserProfiles()});
        } else {
            invoke("refreshStats", false);
        }
    }

    public List<BatterySipperWrapper> getUsageList() {
        List list = (List) invoke("getUsageList");
        List<BatterySipperWrapper> sippers = new ArrayList<>();
        for (Object obj : list) {
            sippers.add(new BatterySipperWrapper(obj));
        }
        return sippers;
    }

    public int getTotalPower() {
        return (int) invoke("getTotalPower");
    }

    public int getMaxPower() {
        return (int) invoke("getMaxPower");
    }
}
