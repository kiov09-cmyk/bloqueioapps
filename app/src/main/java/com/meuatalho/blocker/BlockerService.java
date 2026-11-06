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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockerService extends AccessibilityService {

    private static final int[] MARCOS = {10, 5, 2, 1};

    private String homePkg;
    private String currentPkg = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Long> ultimoUnlockVisto = new HashMap<>();
    private final Map<String, Set<Integer>> marcosAvisados = new HashMap<>();

    private final Runnable checker = new Runnable() {
        @Override
        public void run() {
            verificarBloqueio(currentPkg);
            verificarNotificacoes();
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

        NotificationHelper.criarCanal(this);
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

    private void verificarNotificacoes() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        Set<String> blocked = prefs.getStringSet("blocked_apps", new HashSet<>());
        long agora = System.currentTimeMillis();
        PackageManager pm = getPackageManager();

        for (String pkg : blocked) {
            long until = prefs.getLong("unlock_" + pkg, 0);
            if (until <= agora) continue;

            Long ultimoVisto = ultimoUnlockVisto.get(pkg);
            if (ultimoVisto == null || !ultimoVisto.equals(until)) {
                ultimoUnlockVisto.put(pkg, until);
                marcosAvisados.put(pkg, new HashSet<>());
            }
            Set<Integer> avisados = marcosAvisados.get(pkg);

            long restanteMin = (until - agora) / 60000L + 1;
            for (int marco : MARCOS) {
                if (restanteMin <= marco && !avisados.contains(marco)) {
                    avisados.add(marco);
                    String label;
                    try {
                        label = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
                    } catch (Exception e) { label = pkg; }
                    NotificationHelper.avisar(this, label, marco, pkg.hashCode() + marco);
                }
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
