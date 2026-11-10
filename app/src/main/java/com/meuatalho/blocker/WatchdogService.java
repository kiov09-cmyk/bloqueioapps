package com.meuatalho.blocker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WatchdogService extends Service {

    private static final String TAG = "WatchdogService";
    private static final String CHANNEL = "watchdog";

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable checagem = new Runnable() {
        @Override
        public void run() {
            verificar();
            handler.postDelayed(this, ConfigHelper.getIntervaloWatchdog(WatchdogService.this));
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanal();
        startForeground(9001, montarNotificacao());
        handler.removeCallbacks(checagem);
        handler.post(checagem);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(checagem);
    }

    private void verificar() {
        boolean ativo = servicoDeAcessibilidadeAtivo();
        if (!ativo) {
            Log.d(TAG, "BlockerService nao esta ativo, zerando liberacoes e indo para Home");
            zerarLiberacoes();
            irParaHome();
        }
    }

    private boolean servicoDeAcessibilidadeAtivo() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        List<AccessibilityServiceInfo> lista =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : lista) {
            String id = info.getId();
            if (id != null && id.contains("com.meuatalho.blocker") && id.contains("BlockerService")) {
                return true;
            }
        }
        return false;
    }

    private void zerarLiberacoes() {
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE | MODE_MULTI_PROCESS);
        Set<String> blocked = prefs.getStringSet("blocked_apps", new HashSet<>());
        SharedPreferences.Editor ed = prefs.edit();
        for (String pkg : blocked) {
            ed.putLong("unlock_" + pkg, 0L);
        }
        ed.apply();
    }

    private void irParaHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
    }

    private void criarCanal() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL, "Monitor de bloqueio", NotificationManager.IMPORTANCE_MIN);
            nm.createNotificationChannel(ch);
        }
    }

    private Notification montarNotificacao() {
        return new Notification.Builder(this, CHANNEL)
                .setContentTitle("Bloqueio de Apps ativo")
                .setContentText("Monitorando o serviço de acessibilidade")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setOngoing(true)
                .build();
    }
}
