package com.meuatalho.blocker;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlockerService extends AccessibilityService {

    private static final String TAG = "BlockerService";
    private static final int[] MARCOS = {10, 5, 2, 1};

    private String homePkg;
    private String currentPkg = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, Long> ultimoUnlockVisto = new HashMap<>();
    private final Map<String, Set<Integer>> marcosAvisados = new HashMap<>();

    private WindowManager wm;
    private TextView overlayView;
    private final Handler overlayHandler = new Handler(Looper.getMainLooper());
    private final Runnable esconderAviso = () -> {
        if (overlayView != null) {
            try { wm.removeView(overlayView); } catch (Exception ignored) {}
            overlayView = null;
        }
    };

    private final Runnable checker = new Runnable() {
        @Override
        public void run() {
            verificarBloqueio(currentPkg);
            verificarAvisos();
            handler.postDelayed(this, 12000);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        PackageManager pm = getPackageManager();
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo ri = pm.resolveActivity(home, 0);
        homePkg = (ri != null && ri.activityInfo != null) ? ri.activityInfo.packageName : null;
        Log.d(TAG, "homePkg=" + homePkg);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        handler.removeCallbacks(checker);
        handler.post(checker);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence pkgCs = event.getPackageName();
        if (pkgCs == null) return;
        currentPkg = pkgCs.toString();
        Log.d(TAG, "janela mudou para: " + currentPkg);
        verificarBloqueio(currentPkg);
    }

    private void verificarBloqueio(String pkg) {
        if (pkg == null || pkg.isEmpty()) return;
        if (pkg.equals(getPackageName())) return;
        if (pkg.equals(homePkg)) return;
        if (pkg.equals("com.android.settings") || pkg.equals("com.android.tv.settings")) return;

        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE | MODE_MULTI_PROCESS);
        Set<String> blocked = prefs.getStringSet("blocked_apps", new HashSet<>());
        if (blocked.contains(pkg)) {
            long until = prefs.getLong("unlock_" + pkg, 0);
            long agora = System.currentTimeMillis();
            Log.d(TAG, "pkg bloqueado=" + pkg + " until=" + until + " agora=" + agora + " expirado=" + (agora > until));
            if (agora > until) {
                boolean ok = performGlobalAction(GLOBAL_ACTION_HOME);
                Log.d(TAG, "performGlobalAction HOME retornou=" + ok);
            }
        }
    }

    private void verificarAvisos() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE | MODE_MULTI_PROCESS);
        Set<String> blocked = prefs.getStringSet("blocked_apps", new HashSet<>());
        long agora = System.currentTimeMillis();
        PackageManager pm = getPackageManager();

        for (String pkg : blocked) {
            long until = prefs.getLong("unlock_" + pkg, 0);
            if (until <= agora) continue;

            Long ultimoVisto = ultimoUnlockVisto.get(pkg);
            if (ultimoVisto == null || !ultimoVisto.equals(until)) {
                ultimoUnlockVisto.put(pkg, until);
                int totalMin = prefs.getInt("minutes_" + pkg, 10);
                Set<Integer> jaPassados = new HashSet<>();
                for (int m : MARCOS) if (m >= totalMin) jaPassados.add(m);
                marcosAvisados.put(pkg, jaPassados);
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
                    String texto = marco <= 1 ? label + ": menos de 1 minuto" : label + ": faltam " + marco + " minutos";
                    mostrarAviso(texto);
                    tocarSom();
                }
            }
        }
    }

    private void mostrarAviso(String texto) {
        overlayHandler.removeCallbacks(esconderAviso);
        if (overlayView == null) {
            overlayView = new TextView(this);
            overlayView.setBackgroundColor(0x99000000);
            overlayView.setTextColor(0xFFFFFFFF);
            overlayView.setTextSize(24);
            overlayView.setPadding(60, 36, 60, 36);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            lp.y = 80;
            try {
                wm.addView(overlayView, lp);
            } catch (Exception e) {
                Log.d(TAG, "falha ao mostrar overlay: " + e);
                overlayView = null;
                return;
            }
        }
        overlayView.setText(texto);
        overlayHandler.postDelayed(esconderAviso, 6000);
    }

    private void tocarSom() {
        try {
            SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE | MODE_MULTI_PROCESS);
            String uriStr = prefs.getString("alert_sound_uri", null);
            if (uriStr == null) return;
            int volPct = prefs.getInt("alert_volume", 50);
            float vol = Math.max(0f, Math.min(1f, volPct / 100f));

            MediaPlayer mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mp.setDataSource(this, Uri.parse(uriStr));
            mp.setVolume(vol, vol);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            Log.d(TAG, "falha ao tocar som: " + e);
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checker);
        overlayHandler.removeCallbacks(esconderAviso);
        if (overlayView != null) { try { wm.removeView(overlayView); } catch (Exception ignored) {} }
    }
}
