package com.lexing.batterytest.batteryhelper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.HashMap;

/**
 * Created by Ray on 2017/1/18.
 */
public class BatteryInfoUtils {

    private static final HashMap<String, BatteryInfo> mUidCache =
            new HashMap<>();

    public static void fillNameNIcon(Context context, BatteryInfo batteryInfo, String pkgName){
        getQuickNameIcon(context, batteryInfo, pkgName);
    }

    public static void fillNameNIconNPkg(Context context, BatteryInfo batteryInfo, int uid){
        final String uidString = Integer.toString(uid);
        if (mUidCache.containsKey(uidString)) {
            BatteryInfo batteryInfoCache = mUidCache.get(uidString);
            batteryInfo.setPkgName(batteryInfoCache.getPkgName());
            batteryInfo.setName(batteryInfoCache.getName());
            batteryInfo.setIcon(batteryInfoCache.getIcon());
            return;
        }
        getQuickNameIconForUid(context, batteryInfo, uid);
    }

    private static void getQuickNameIcon(Context context, BatteryInfo batteryInfo, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, 0);
            batteryInfo.setIcon(appInfo.loadIcon(pm));// pm.getApplicationIcon(appInfo);
            batteryInfo.setName(appInfo.loadLabel(pm).toString());// pm.getApplicationLabel(appInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void getQuickNameIconForUid(Context context, BatteryInfo batteryInfo, int uid) {
        PackageManager pm = context.getPackageManager();
        String[] packages = pm.getPackagesForUid(uid);
//		Drawable icon = pm.getDefaultActivityIcon();
        if (packages == null) {
            // name = Integer.toString(uid);
            if (uid == 0) {
                batteryInfo.setDrainType(BatterySipperWrapper.DrainTypeWrapper.KERNEL);
                // name = mContext.getResources().getString(R.string.process_kernel_label);
            } else if ("mediaserver".equals(batteryInfo.getName())) {
                batteryInfo.setDrainType(BatterySipperWrapper.DrainTypeWrapper.MEDIASERVER);
                // name = mContext.getResources().getString(R.string.process_mediaserver_label);
            }
            // iconId = R.drawable.ic_power_system;
            // icon = mContext.getResources().getDrawable(iconId);
            return;
        }

        getNameIcon(context, batteryInfo, uid);
    }

    /**
     * Sets name and icon
     */
    private static void getNameIcon(Context context, BatteryInfo batteryInfo, int uid) {
        PackageManager pm = context.getPackageManager();
        final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
        String[] packages = pm.getPackagesForUid(uid);
        String defaultPackageName = null;
        Drawable icon = null;
        String name = null;
        if (packages == null) {
            batteryInfo.setName(Integer.toString(uid));
            return;
        }

        String[] packageLabels = new String[packages.length];
        System.arraycopy(packages, 0, packageLabels, 0, packages.length);

        // Convert package names to user-facing labels where possible
        for (int i = 0; i < packageLabels.length; i++) {
            // Check if package matches preferred package
            try {
                ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
                CharSequence label = ai.loadLabel(pm);
                if (label != null) {
                    packageLabels[i] = label.toString();
                }
                if (ai.icon != 0) {
                    defaultPackageName = packages[i];
                    icon = ai.loadIcon(pm);
                    break;
                }
            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        if (icon == null)
            icon = defaultActivityIcon;

        if (packageLabels.length == 1) {
            name = packageLabels[0];
        } else {
            // Look for an official name for this UID.
            for (String pkgName : packages) {
                try {
                    final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
                    if (pi.sharedUserLabel != 0) {
                        final CharSequence nm = pm.getText(pkgName, pi.sharedUserLabel, pi.applicationInfo);
                        if (nm != null) {
                            name = nm.toString();
                            if (pi.applicationInfo.icon != 0) {
                                defaultPackageName = pkgName;
                                icon = pi.applicationInfo.loadIcon(pm);
                            }
                            break;
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
        }
        final String uidString = Integer.toString(uid);
        batteryInfo.setName(name);
        batteryInfo.setIcon(icon);
        batteryInfo.setPkgName(defaultPackageName);
        mUidCache.put(uidString, batteryInfo.clone());
    }
}
