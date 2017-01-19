package com.lexing.batterytest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lexing.batterytest.batteryhelper.BatteryInfo;
import com.lexing.batterytest.batteryhelper.BatteryInfoUtils;
import com.lexing.batterytest.batteryhelper.BatterySipperWrapper;
import com.lexing.batterytest.batteryhelper.BatteryStatsHelperWrapper;
import com.lexing.batterytest.batteryhelper.IBatteryStatsConst;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ray on 2017/1/17.
 */
public class MainActivity extends Activity {

    private static final int MENU_STATS_REFRESH = Menu.FIRST + 1;
    static final int MSG_REFRESH_STATS = 100;

    private TextView batterySummary;
    private ListView listView;
    private CustomAdapter adapter;
    private BatteryStatsHelperWrapper mStatsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatsHelper = BatteryStatsHelperWrapper.create(this, true);
        mStatsHelper.create(savedInstanceState);
        batterySummary = (TextView) findViewById(R.id.batterySummary);
        listView = (ListView) findViewById(R.id.listview);
        adapter = new CustomAdapter();
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStatsHelper.clearStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        BatteryStatsHelperWrapper.dropFile(this, "tmp_bat_history.bin");
        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (mHandler.hasMessages(MSG_REFRESH_STATS)) {
            mHandler.removeMessages(MSG_REFRESH_STATS);
            mStatsHelper.clearStats();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBatteryInfoReceiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler.removeMessages(MSG_REFRESH_STATS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isChangingConfigurations()){
            mStatsHelper.storeState();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem refresh = menu.add(0, MENU_STATS_REFRESH, 0, "refresh")
                .setIcon(android.R.drawable.stat_notify_sync)
                .setAlphabeticShortcut('r');
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
            case MENU_STATS_REFRESH:
                    mStatsHelper.clearStats();
                    refreshStats();
                    mHandler.removeMessages(MSG_REFRESH_STATS);
                    return true;
        }
       return super.onOptionsItemSelected(item);
    }

    protected void refreshStats() {
        mStatsHelper.refreshStats(IBatteryStatsConst.STATS_SINCE_CHARGED);
        List<BatterySipperWrapper> usageList = mStatsHelper.getUsageList();
        List<BatteryInfo> batteryInfos;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            batteryInfos = parseSipperV21(usageList);
        } else {
            batteryInfos = parseSipper(usageList);
        }
        adapter.setData(batteryInfos);
    }

    private List<BatteryInfo> parseSipper(List<BatterySipperWrapper> usageList) {
        List<BatteryInfo> batteryInfos = new ArrayList<>();
        BatteryInfo batteryInfo;
        for (BatterySipperWrapper sipper : usageList) {
            batteryInfo = new BatteryInfo();
            if (sipper.getSortValue() < BatteryStatsHelperWrapper.MIN_POWER_THRESHOLD) continue;
            final double percentOfTotal =
                    ((sipper.getSortValue() / mStatsHelper.getTotalPower()) * 100);
            if (percentOfTotal < 1) continue;
            batteryInfo.setPercentOfTotal(percentOfTotal);
            batteryInfo.setDrainType(sipper.getDrainType());
            BatteryInfoUtils.fillNameNIconNPkg(this, batteryInfo, sipper.getUid());
        }
        return batteryInfos;
    }

    private List<BatteryInfo> parseSipperV21(List<BatterySipperWrapper> usageList) {
        return null;
    }

    class CustomAdapter extends BaseAdapter {
        private List<BatteryInfo> list;
        private LayoutInflater inflater;

        CustomAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        void setData(List<BatteryInfo> batteryInfos) {
            for (int i = batteryInfos.size() - 1; i >= 0; i--) {
                final BatteryInfo batteryInfo = batteryInfos.get(i);
                String name = batteryInfo.getName();
                if (name == null) {
                    Drawable icon = batteryInfo.getIcon();
                    switch (batteryInfo.getDrainType()) {
                        case CELL:
                            name = getString(R.string.power_cell);
                            icon = getResources().getDrawable(R.drawable.ic_settings_cell_standby);
                            break;
                        case IDLE:
                            name = getString(R.string.power_idle);
                            icon = getResources().getDrawable(R.drawable.ic_settings_phone_idle);
                            break;
                        case BLUETOOTH:
                            name = getString(R.string.power_bluetooth);
                            icon = getResources().getDrawable(R.drawable.ic_settings_bluetooth);
                            break;
                        case WIFI:
                            name = getString(R.string.power_wifi);
                            icon = getResources().getDrawable(R.drawable.ic_settings_wifi);
                            break;
                        case SCREEN:
                            name = getString(R.string.power_screen);
                            icon = getResources().getDrawable(R.drawable.ic_settings_display);
                            break;
                        case PHONE:
                            name = getString(R.string.power_phone);
                            icon = getResources().getDrawable(R.drawable.ic_settings_voice_calls);
                            break;
                        case KERNEL:
                            name = getString(R.string.process_kernel_label);
                            icon = getResources().getDrawable(R.drawable.ic_power_system);
                            break;
                        case MEDIASERVER:
                            name = getString(R.string.process_mediaserver_label);
                            icon = getResources().getDrawable(R.drawable.ic_power_system);
                            break;
                        default:
                            break;
                    }

                    if (name != null) {
                        batteryInfo.setName(name);
                        if (icon == null) {
                            PackageManager pm = getPackageManager();
                            icon = pm.getDefaultActivityIcon();
                        }
                        batteryInfo.setIcon(icon);
                    } else {
                        batteryInfos.remove(i);
                    }
                }
            }
            this.list = batteryInfos;
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public BatteryInfo getItem(int position) {
            return list == null ? null : list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = inflater.inflate(R.layout.listview_item, null);
                holder.appIcon = (ImageView) convertView.findViewById(R.id.appIcon);
                holder.appName = (TextView) convertView.findViewById(R.id.appName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            BatteryInfo sipper = getItem(position);
            holder.appName.setText(sipper.getName());
            holder.appIcon.setImageDrawable(sipper.getIcon());

            double percentOfTotal = sipper.getPercentOfTotal();
            holder.txtProgress.setText(format(percentOfTotal));
            holder.progress.setProgress((int) percentOfTotal);

            return convertView;
        }
    }

    private String format(double size) {
        return String.format("%1$.2f%%", size);
        // return new BigDecimal("" + size).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    class Holder {
        ImageView appIcon;
        TextView appName;
        TextView txtProgress;
        ProgressBar progress;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_STATS:
                    mStatsHelper.clearStats();
                    refreshStats();
                    break;
            }
        }
    };

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                if (!mHandler.hasMessages(MSG_REFRESH_STATS)) {
                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
                }
            }
        }
    };
}
