package com.meuatalho.blocker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;
import java.util.Set;

public class BlockerService extends AccessibilityService {

    private String homePkg;
    private String currentPkg = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable checker = new Runnable() {
        @Override
        public void run() {
            verificarBloqueio(currentPkg);
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        PackageManager pm = getPackageManager();
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo ri = pm.resolveActivity(home, 0);
        homePkg = (ri != null && ri.activityInfo != null) ? ri.activityInfo.packageName : null;

        handler.post(checker);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        currentPkg = pkgCs.toString();
        verificarBloqueio(currentPkg);
    }

    private void verificarBloqueio(String pkg) {
        if (pkg == null || pkg.isEmpty()) return;
        if (pkg.equals(getPackageName())) return;
        if (pkg.equals(homePkg)) return;
        if (pkg.equals("com.android.settings") || pkg.equals("com.android.tv.settings")) return;

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        Set<String> blocked = prefs.getStringSet("blocked_apps", new HashSet<>());
        if (blocked.contains(pkg)) {
            long until = prefs.getLong("unlock_" + pkg, 0);
            if (System.currentTimeMillis() > until) {
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checker);
    }
}
