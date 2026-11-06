package com.meuatalho.blocker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationHelper {
    private static final String CHANNEL = "tempo_restante";

    public static void criarCanal(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            NotificationChannel ch = new NotificationChannel(CHANNEL, "Tempo restante",
                    NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(ch);
        }
    }

    public static void avisar(Context ctx, String appLabel, int minutos, int id) {
        String texto = minutos <= 1 ? "falta menos de 1 minuto" : "faltam " + minutos + " minutos";
        Notification n = new Notification.Builder(ctx, CHANNEL)
                .setContentTitle(appLabel + " vai bloquear em breve")
                .setContentText("Tempo liberado: " + texto)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setAutoCancel(true)
                .build();
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, n);
    }
}
