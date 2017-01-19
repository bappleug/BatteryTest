package com.lexing.batterytest.batteryhelper;

import android.annotation.TargetApi;
import android.os.Build;

/**
 * Created by Ray on 2017/1/18.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class BatterySipperWrapper extends Wrapper{

    public enum DrainTypeWrapper {
        IDLE, CELL, PHONE, WIFI, BLUETOOTH, SCREEN, APP, KERNEL, MEDIASERVER;
    }

    protected BatterySipperWrapper(Object wrappedObj) {
        super(wrappedObj);
    }

    @Override
    protected String className() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            return "com.android.internal.os.BatterySipper";
        } else {
            return "com.android.settings.fuelgauge.BatterySipper";
        }
    }

    public int getUid() {
        return (int) invoke("getUid");
    }

    public DrainTypeWrapper getDrainType() {
        Object drainTypeObj = invoke("getDrainType");
        try {
            return DrainTypeWrapper.valueOf(
                    (String) ReflectHelper.invokeNoStaticMethod(drainTypeObj, "getName", null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getSortValue() {
        return (int) invoke("getSortValue");
    }

}
