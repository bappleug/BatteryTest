package com.lexing.batterytest.batteryhelper.v18;

import android.os.RemoteException;

import com.lexing.batterytest.batteryhelper.Wrapper;

/**
 * Created by Ray on 2017/1/16.
 */
public class IBatteryStatsV18 extends Wrapper {

    private IBatteryStatsV18(Object batteryStatsStub){
        super(batteryStatsStub);
    }

    @Override
    protected String className() {
        return "com.android.internal.app.IBatteryStatsConst";
    }

    public static IBatteryStatsV18 create() {
//			mBatteryStats = IBatteryStatsConst.Stub.asInterface(ServiceManager.getService("batteryinfo"));
        try{
            Object batteryStatsService;
            batteryStatsService = Class.forName("android.os.ServiceManager")
                    .getMethod("getService", String.class)
                    .invoke(null, "batteryinfo");
            Object batteryStatsStub = Class.forName("com.android.internal.app.IBatteryStatsConst$Stub")
                    .getMethod("asInterface", android.os.IBinder.class)
                    .invoke(null, batteryStatsService);
            return new IBatteryStatsV18(batteryStatsStub);
        } catch (Exception e){
            handleException(e);
        }
        return null;
    }

    public byte[] getStatistics() throws RemoteException {
        return (byte[]) invoke("getStatistics");
    }
}
