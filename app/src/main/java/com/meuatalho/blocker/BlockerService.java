package com.meuatalho.blocker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.accessibility.AccessibilityEvent;

import java.util.HashSet;
import java.util.Set;

public class BlockerService extends AccessibilityService {

    private String homePkg;

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
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        String pkg = pkgCs.toString();

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
}
