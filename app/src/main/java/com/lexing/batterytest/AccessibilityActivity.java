package com.lexing.batterytest;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Ray on 2017/3/1.
 */

public class AccessibilityActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessibility);


        this.findViewById(R.id.activeButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent killIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(killIntent);
            }
        });

        this.findViewById(R.id.installButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MyAccessibilityService.INVOKE_TYPE = MyAccessibilityService.TYPE_INSTALL_APP;
                String fileName = Environment.getExternalStorageDirectory() + "/test.apk";
                File installFile = new File(fileName);
                if (installFile.exists()) {
                    installFile.delete();
                }
                try {
                    installFile.createNewFile();
                    FileOutputStream out = new FileOutputStream(installFile);
                    byte[] buffer = new byte[512];
                    InputStream in = AccessibilityActivity.this.getAssets().open("test.apk");
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                    in.close();
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                startActivity(intent);

            }
        });
        this.findViewById(R.id.uninstallButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MyAccessibilityService.INVOKE_TYPE = MyAccessibilityService.TYPE_UNINSTALL_APP;
                Uri packageURI = Uri.parse("package:io.amosbake.animationsummary");
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                startActivity(uninstallIntent);
            }
        });
        this.findViewById(R.id.killAppButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MyAccessibilityService.INVOKE_TYPE = MyAccessibilityService.TYPE_KILL_APP;
                Intent killIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri packageURI = Uri.parse("package:io.amosbake.animationsummary");
                killIntent.setData(packageURI);
                startActivity(killIntent);
            }
        });
    }

    private void checkServiceState() {
        boolean serviceEnabled = false;
        // 循环遍历所有服务，查看是否开启
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getResolveInfo().serviceInfo.packageName.equals(getPackageName())) {
                serviceEnabled = true;
                break;
            }
        }
        if (serviceEnabled) {
            ((TextView)findViewById(R.id.txtServiceState)).setText("服务已开启");
            // Prevent screen from dimming
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            ((TextView)findViewById(R.id.txtServiceState)).setText("服务未开启");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkServiceState();
    }
}