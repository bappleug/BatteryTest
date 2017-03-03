package com.lexing.batterytest;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.lexing.batterytest.batteryhelper.AppStatsHelper;
import com.lexing.batterytest.batteryhelper.AutoBootAppInfo;
import com.lexing.batterytest.batteryhelper.BatterySipper;
import com.lexing.batterytest.batteryhelper.BatteryStatsHelper;
import com.lexing.batterytest.batteryhelper.RunningAppInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {
    private final int PROGRESS_DIALOG_ID = 1;

    private TextView batterySummary;
    private ListView listView;
    private AmAdapter amAdapter;
    private FileAdapter fileAdapter;
    private AutoBootAdapter autoBootAdapter;
    private RunningServiceAdapter runningServiceAdapter;
    private LibAdapter libAdapter;

    private BatteryStatsHelper batteryStatsHelper;
    private AppStatsHelper appStatsHelper;
    private ProgressDialog progressDialog;

    private List<BatterySipper> mAmList;
    private List<RunningAppInfo> mFileList;
    private List<AutoBootAppInfo> mAutoBootList;
    private List<RunningAppInfo> mRunningServiceInfos;
    private List<RunningAppInfo> mAppsFromLibList;
    private String mBatterySummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_below_jellybean);

        batterySummary = (TextView) findViewById(R.id.batterySummary);
        listView = (ListView) findViewById(R.id.listview);
        amAdapter = new AmAdapter();
        fileAdapter = new FileAdapter();
        autoBootAdapter = new AutoBootAdapter();
        runningServiceAdapter = new RunningServiceAdapter();
        libAdapter = new LibAdapter();

        batteryStatsHelper = new BatteryStatsHelper();
        batteryStatsHelper.setMinPercentOfTotal(0.01f);
        appStatsHelper = new AppStatsHelper();
        mRunningServiceInfos = new ArrayList<>();
        mAppsFromLibList = new ArrayList<>();

        registerReceiver(mBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        getAmApps();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_apps_from_am:
                getAmApps();
                break;
            case R.id.menu_apps_from_file:
                getFileApps();
                break;
            case R.id.menu_apps_auto_boot:
                getAutoBoot();
                break;
            case R.id.menu_running_services:
                getRunningService();
                break;
            case R.id.menu_apps_from_lib:
                getAppsByLib();
                break;
            case R.id.menu_remove_task:
//                ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//                manager.removeTasks();
                Toast.makeText(this, "无效", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_kill_process:
                ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                for (BatterySipper batterySipper : mAmList) {
                    if (batterySipper.getPkgName().equals(this.getPackageName())) {
                        continue;
                    }
                    manager.killBackgroundProcesses(batterySipper.getPkgName());
                }
                break;
            case R.id.menu_accessibility:
                Intent intent = new Intent(this, AccessibilityActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void getAppsByLib() {
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
        listView.setAdapter(libAdapter);
        libAdapter.setData(processes);
        batterySummary.setText(mBatterySummary + "\n测试信息：通过第三方库获取RunningApp");
    }

    private void getRunningService() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceInfos = manager.getRunningServices(100);
        listView.setAdapter(runningServiceAdapter);
        runningServiceAdapter.setData(runningServiceInfos);
        batterySummary.setText(mBatterySummary + "\n测试信息：获取RunningServices");
    }

    private void getAutoBoot() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> appInfo = pm.getInstalledApplications(0);
        Iterator<ApplicationInfo> appInfoIterator = appInfo.iterator();
        List<AutoBootAppInfo> appList = new ArrayList<>();
        while (appInfoIterator.hasNext()) {
            ApplicationInfo app = appInfoIterator.next();
            int flag = pm.checkPermission("android.permission.RECEIVE_BOOT_COMPLETED", app.packageName);
            if (flag == PackageManager.PERMISSION_GRANTED) {
                AutoBootAppInfo autoBootAppInfo = new AutoBootAppInfo();
                String label = pm.getApplicationLabel(app).toString();
                Drawable icon = pm.getApplicationIcon(app);
                String desc = app.packageName;
                autoBootAppInfo.name = label;
                autoBootAppInfo.icon = icon;
                autoBootAppInfo.desc = desc;
                appList.add(autoBootAppInfo);
            }
        }
        Utils.getAutoBootsNum(MainActivity.this, appList);
        listView.setAdapter(autoBootAdapter);
        autoBootAdapter.setData(appList);
        batterySummary.setText(mBatterySummary + "\n测试信息：获取Receive_Boot权限的应用");
    }

    private void getAmApps() {
        showDialog(PROGRESS_DIALOG_ID);
        new Thread() {
            public void run() {
                mAmList = batteryStatsHelper.getBatteryStats(MainActivity.this);
                for (BatterySipper sipper : mAmList) {
                    Utils.getQuickNameIcon(MainActivity.this, sipper);
                }
                mHandler.sendEmptyMessage(1);
            }
        }.start();
    }

    private void getFileApps() {
        showDialog(PROGRESS_DIALOG_ID);
        new Thread() {
            public void run() {
                mFileList = appStatsHelper.readAppStats(MainActivity.this, false);
                for (RunningAppInfo appInfo : mFileList) {
                    Utils.getQuickNameIcon(MainActivity.this, appInfo);
                }
                mHandler.sendEmptyMessage(2);
            }
        }.start();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (isFinishing())
                        return;
                    progressDialog.dismiss();
                    batterySummary.setText(mBatterySummary + "\n测试信息：获取方式(5.0以下通过ActivityManager)");
                    listView.setAdapter(amAdapter);
                    amAdapter.setData(mAmList);
                    break;
                case 2:
                    if (isFinishing())
                        return;
                    progressDialog.dismiss();
                    batterySummary.setText(mBatterySummary + "\n测试信息：获取方式(5.0以上通过系统文件)");
                    listView.setAdapter(fileAdapter);
                    fileAdapter.setData(mFileList);
                    break;
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG_ID:
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("请稍候...");
                return progressDialog;
        }
        return null;
    }

    class AmAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public AmAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        public void setData(List<BatterySipper> sipperList) {

            mAmList = sipperList;
            for (int i = mAmList.size() - 1; i >= 0; i--) {
                final BatterySipper sipper = mAmList.get(i);
                String name = sipper.getName();
                Drawable icon = sipper.getIcon();
                if (name != null) {
                    if (icon == null) {
                        PackageManager pm = getPackageManager();
                        icon = pm.getDefaultActivityIcon();
                        sipper.setIcon(icon);
                    }
                } else {
                    mAmList.remove(i);
                }
            }
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mAmList == null ? 0 : mAmList.size();
        }

        @Override
        public BatterySipper getItem(int position) {
            return mAmList == null ? null : mAmList.get(position);
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
                holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            BatterySipper appInfo = getItem(position);
            holder.appName.setText(appInfo.getName());
            holder.appIcon.setImageDrawable(appInfo.getIcon());
            holder.tvPackageName.setText(appInfo.getPkgName());

            double percentOfTotal = appInfo.getPercentOfTotal();
            holder.txtProgress.setText(format(percentOfTotal));
            holder.progress.setProgress((int) percentOfTotal);

            return convertView;
        }
    }

    class FileAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public FileAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        public void setData(List<RunningAppInfo> sipperList) {
            mFileList = sipperList;
            for (int i = mFileList.size() - 1; i >= 0; i--) {
                final RunningAppInfo appInf = mFileList.get(i);
                String name = appInf.name;
                Drawable icon = appInf.icon;
                if (name != null) {
                    if (icon == null) {
                        PackageManager pm = getPackageManager();
                        icon = pm.getDefaultActivityIcon();
                        appInf.icon = icon;
                    }
                } else {
                    mFileList.remove(i);
                }
            }
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mFileList == null ? 0 : mFileList.size();
        }

        @Override
        public RunningAppInfo getItem(int position) {
            return mFileList == null ? null : mFileList.get(position);
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
                holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            RunningAppInfo appInfo = getItem(position);
            holder.appName.setText(appInfo.name);
            holder.appIcon.setImageDrawable(appInfo.icon);
            holder.tvPackageName.setText(appInfo.pkgName);

            double percentOfTotal = appInfo.cpuPercent;
            holder.txtProgress.setText(format(percentOfTotal));
            holder.progress.setProgress((int) percentOfTotal);

            return convertView;
        }
    }

    class AutoBootAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public AutoBootAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        public void setData(List<AutoBootAppInfo> autoBootAppInfos) {
            mAutoBootList = autoBootAppInfos;
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mAutoBootList == null ? 0 : mAutoBootList.size();
        }

        @Override
        public AutoBootAppInfo getItem(int position) {
            return mAutoBootList == null ? null : mAutoBootList.get(position);
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
                holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            AutoBootAppInfo autoBootAppInfo = getItem(position);
            holder.appName.setText(autoBootAppInfo.name);
            holder.appIcon.setImageDrawable(autoBootAppInfo.icon);
            holder.tvPackageName.setText(autoBootAppInfo.runningTime);

            holder.txtProgress.setText(autoBootAppInfo.boots);
            holder.progress.setProgress(0);

            return convertView;
        }
    }

    private class RunningServiceAdapter extends BaseAdapter{
        private LayoutInflater inflater;

        public RunningServiceAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        public void setData(List<ActivityManager.RunningServiceInfo> serviceInfos) {
            mRunningServiceInfos.clear();
            for (int i = serviceInfos.size() - 1; i >= 0; i--) {
                RunningAppInfo appInfo = new RunningAppInfo();
                appInfo.pkgName = serviceInfos.get(i).service.getPackageName();
                Utils.getQuickNameIcon(MainActivity.this, appInfo);
                String name = appInfo.name;
                Drawable icon = appInfo.icon;
                if (name != null) {
                    if (icon == null) {
                        PackageManager pm = getPackageManager();
                        icon = pm.getDefaultActivityIcon();
                        appInfo.icon = icon;
                    }
                } else {
                    mFileList.remove(i);
                }
                mRunningServiceInfos.add(appInfo);
            }
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mRunningServiceInfos == null ? 0 : mRunningServiceInfos.size();
        }

        @Override
        public RunningAppInfo getItem(int position) {
            return mRunningServiceInfos == null ? null : mRunningServiceInfos.get(position);
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
                holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            RunningAppInfo appInfo = getItem(position);
            holder.appName.setText(appInfo.name);
            holder.appIcon.setImageDrawable(appInfo.icon);
            holder.tvPackageName.setText(appInfo.pkgName);

            holder.txtProgress.setText("0%");
            holder.progress.setProgress(0);

            return convertView;
        }
    }

    private class LibAdapter extends BaseAdapter{
        private LayoutInflater inflater;

        public LibAdapter() {
            inflater = LayoutInflater.from(MainActivity.this);
        }

        public void setData(List<AndroidAppProcess> appProcesses) {
            mRunningServiceInfos.clear();
            for (int i = appProcesses.size() - 1; i >= 0; i--) {
                RunningAppInfo appInfo = new RunningAppInfo();
                appInfo.pkgName = appProcesses.get(i).getPackageName();
                Utils.getQuickNameIcon(MainActivity.this, appInfo);
                String name = appInfo.name;
                Drawable icon = appInfo.icon;
                if (name != null) {
                    if (icon == null) {
                        PackageManager pm = getPackageManager();
                        icon = pm.getDefaultActivityIcon();
                        appInfo.icon = icon;
                    }
                } else {
                    mFileList.remove(i);
                }
                mRunningServiceInfos.add(appInfo);
            }
            notifyDataSetInvalidated();
        }

        @Override
        public int getCount() {
            return mRunningServiceInfos == null ? 0 : mRunningServiceInfos.size();
        }

        @Override
        public RunningAppInfo getItem(int position) {
            return mRunningServiceInfos == null ? null : mRunningServiceInfos.get(position);
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
                holder.tvPackageName = (TextView) convertView.findViewById(R.id.tvPackageName);
                holder.txtProgress = (TextView) convertView.findViewById(R.id.txtProgress);
                holder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            RunningAppInfo appInfo = getItem(position);
            holder.appName.setText(appInfo.name);
            holder.appIcon.setImageDrawable(appInfo.icon);
            holder.tvPackageName.setText(appInfo.pkgName);

            holder.txtProgress.setText("0%");
            holder.progress.setProgress(0);

            return convertView;
        }
    }

    class Holder {
        ImageView appIcon;
        TextView appName;
        TextView txtProgress;
        ProgressBar progress;
        TextView tvPackageName;
    }

    private String format(double size) {
        return String.format("%1$.2f%%", size);
        // return new BigDecimal("" + size).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBatteryInfoReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                String batteryLevel = Utils.getBatteryPercentage(intent);
                String batteryStatus = Utils.getBatteryStatus(MainActivity.this.getResources(), intent);
                mBatterySummary = context.getResources().getString(R.string.power_usage_level_and_status, batteryLevel, batteryStatus);
            }
        }
    };


}
