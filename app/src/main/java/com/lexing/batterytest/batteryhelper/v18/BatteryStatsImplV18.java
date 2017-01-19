package com.lexing.batterytest.batteryhelper.v18;

import android.os.Parcel;
import android.util.SparseArray;

import com.lexing.batterytest.batteryhelper.IBatteryStatsConst;
import com.lexing.batterytest.batteryhelper.Wrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ray on 2017/1/16.
 */
public class BatteryStatsImplV18 extends Wrapper implements IBatteryStatsConst {


    private BatteryStatsImplV18(Object batteryStatsImpl){
        super(batteryStatsImpl);
    }

    @Override
    protected String className() {
        return "com.android.internal.os.BatteryStatsImpl";
    }

    public static BatteryStatsImplV18 create(Parcel parcel) {
//		BatteryStatsImpl.CREATOR.createFromParcel(parcel);
        Object batteryStatsImpl = null;
        try {
            Object creator = Class.forName("com.android.internal.os.BatteryStatsImpl")
                    .getField("CREATOR").get(null);
            batteryStatsImpl = creator.getClass().getMethod("createFromParcel", Parcel.class)
                    .invoke(creator, parcel);
        } catch (Exception e) {
            handleException(e);
        }
        return new BatteryStatsImplV18(batteryStatsImpl);
    }

    public void distributeWorkLocked(int which) {
        invoke("distributeWorkLocked", which);
    }

    public long computeBatteryRealtime(long uSecTime, int which) {
        return (long) invoke("computeBatteryRealtime", uSecTime, which);
    }

    public long getPhoneOnTime(long uSecNow, int statsType){
        return (long) invoke("getPhoneOnTime", uSecNow, statsType);
    }

    public long getScreenOnTime(long uSecNow, int statsType){
        return (long) invoke("getScreenOnTime", uSecNow, statsType);
    }

    public long getScreenBrightnessTime(int brightnessBin, long elapsedRealtimeUs, int which){
        return (long) invoke("getScreenBrightnessTime", brightnessBin, elapsedRealtimeUs, which);
    }

    public long getWifiOnTime(long elapsedRealtimeUs, int which){
        return (long) invoke("getWifiOnTime", elapsedRealtimeUs, which);
    }

    public long getGlobalWifiRunningTime(long elapsedRealtimeUs, int which){
        return (long) invoke("getGlobalWifiRunningTime", elapsedRealtimeUs, which);
    }

    public long getBluetoothOnTime(long elapsedRealtimeUs, int which){
        return (long) invoke("getBluetoothOnTime", elapsedRealtimeUs, which);
    }

    public int getBluetoothPingCount(){
        return (int) invoke("getBluetoothPingCount");
    }

    public long getPhoneSignalStrengthTime(int strengthBin, long elapsedRealtimeUs, int which){
        return (long) invoke("getPhoneSignalStrengthTime", strengthBin, elapsedRealtimeUs, which);
    }

    public long getPhoneSignalScanningTime(long elapsedRealtimeUs, int which){
        return (long) invoke("getPhoneSignalScanningTime", elapsedRealtimeUs, which);
    }


    public SparseArray<? extends UidWrapper> getUidStats() {
        SparseArray<?> uidArray = (SparseArray) invoke("getUidStats");
        SparseArray<UidWrapper> uidWrapperArray = new SparseArray<>();
        for(int i=0; i < uidArray.size(); i++){
            uidWrapperArray.append(uidArray.keyAt(i), new UidWrapper(uidArray.valueAt(i)));
        }
        return uidWrapperArray;
    }

    public long getMobileTcpBytesReceived(int which) {
        return (long) invoke("getMobileTcpBytesReceived", which);
    }

    public long getMobileTcpBytesSent(int which) {
        return (long) invoke("getMobileTcpBytesSent", which);
    }

    public long getTotalTcpBytesReceived(int which) {
        return (long) invoke("getTotalTcpBytesReceived", which);
    }

    public long getTotalTcpBytesSent(int which) {
        return (long) invoke("getTotalTcpBytesSent", which);
    }

    public long getRadioDataUptime() {
        return (long) invoke("getRadioDataUptime");
    }

    public class UidWrapper extends Wrapper{

        UidWrapper(Object wrappedObj) {
            super(wrappedObj);
        }

        @Override
        protected String className() {
            return "com.android.internal.os.BatteryStatsImpl.Uid";
        }

        public Map<String, ? extends UidWrapper.ProcWrapper> getProcessStats() {
            Map<String, ?> procStats = (Map<String, ?>) invoke("getProcessStats");
            Map<String, ProcWrapper> procWrapperStats = new HashMap<>();
            for(Map.Entry<String, ?> entry : procStats.entrySet()){
                procWrapperStats.put(entry.getKey(), new ProcWrapper(entry.getValue()));
            }
            return procWrapperStats;
        }

        public Map<String, ? extends UidWrapper.WakelockWrapper> getWakelockStats() {
            Map<String, ?> wakelockStats = (Map<String, ?>) invoke("getWakelockStats");
            Map<String, WakelockWrapper> wakelockWrapperStats = new HashMap<>();
            for(Map.Entry<String, ?> entry : wakelockStats.entrySet()){
                wakelockWrapperStats.put(entry.getKey(), new WakelockWrapper(entry.getValue()));
            }
            return wakelockWrapperStats;
        }

        public long getTcpBytesReceived(int mStatsType) {
            return (long) invoke("getTcpBytesReceived", mStatsType);
        }

        public long getTcpBytesSent(int mStatsType) {
            return (long) invoke("getTcpBytesSent", mStatsType);
        }

        public long getWifiRunningTime(long uSecTime, int which) {
            return (long) invoke("getWifiRunningTime", uSecTime, which);
        }

        public Map<Integer, ? extends SensorWrapper> getSensorStats() {
            Map<Integer, ?> sensorStats = (Map<Integer, ?>) invoke("getSensorStats");
            Map<Integer, SensorWrapper> sensorWrapperStats = new HashMap<>();
            for(Map.Entry<Integer, ?> entry : sensorStats.entrySet()){
                sensorWrapperStats.put(entry.getKey(), new SensorWrapper(entry.getValue()));
            }
            return sensorWrapperStats;
        }

        public int getUid() {
            return (int) invoke("getUid");
        }

        public class ProcWrapper extends Wrapper{

            ProcWrapper(Object wrappedObj) {
                super(wrappedObj);
            }

            @Override
            protected String className() {
                return "com.android.internal.os.BatteryStatsImpl.Uid.Proc";
            }

            public long getUserTime(int which) {
                return (long) invoke("getUserTime", which);
            }

            public long getSystemTime(int which) {
                return (long) invoke("getSystemTime", which);
            }

            public long getForegroundTime(int which) {
                return (long) invoke("getForegroundTime", which);
            }

            public long getTimeAtCpuSpeedStep(int step, int which) {
                return (long) invoke("getTimeAtCpuSpeedStep", step, which);
            }
        }

        public class WakelockWrapper extends Wrapper{

            WakelockWrapper(Object wrappedObj) {
                super(wrappedObj);
            }

            @Override
            protected String className() {
                return "com.android.internal.os.BatteryStatsImpl.Uid.Wakelock";
            }

            public TimerWrapper getWakeTime(int wakeTypePartial) {
                Object timer = invoke("getWakeTime", wakeTypePartial);
                if(timer == null){
                    return null;
                } else{
                    return new TimerWrapper(timer);
                }
            }
        }

        public class SensorWrapper extends Wrapper{

            public static final int GPS = -10000;

            SensorWrapper(Object wrappedObj) {
                super(wrappedObj);
            }

            @Override
            protected String className() {
                return "com.android.internal.os.BatteryStatsImpl.Uid.Sensor";
            }

            public int getHandle() {
                return (int) invoke("getHandle");
            }

            public TimerWrapper getSensorTime() {
                Object timer = invoke("getSensorTime");
                if(timer == null){
                    return null;
                } else{
                    return new TimerWrapper(timer);
                }
            }
        }
    }

    public class TimerWrapper extends Wrapper{

        TimerWrapper(Object wrappedObj) {
            super(wrappedObj);
        }

        @Override
        protected String className() {
            return "com.android.internal.os.BatteryStatsImpl.Timer";
        }

        public long getTotalTimeLocked(long batteryRealtime, int which) {
            return (long) invoke("getTotalTimeLocked", batteryRealtime, which);
        }
    }
}
