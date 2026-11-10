package com.meuatalho.blocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Context appContext = context.getApplicationContext();

        Intent watchdog = new Intent(appContext, WatchdogService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            appContext.startForegroundService(watchdog);
        } else {
            appContext.startService(watchdog);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AccessibilityHelper.ativar(appContext);
        }, 15000);
    }
}
