package com.meuatalho.blocker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigHelper {

    public static final long MIN_INTERVALO_MS = 5000L;
    private static final int[] MARCOS_PADRAO = {10, 5, 2, 1};
    private static final long BLOCKER_PADRAO_MS = 12000L;
    private static final long WATCHDOG_PADRAO_MS = 15000L;

    public static int[] getMarcos(Context ctx) {
        SharedPreferences prefs = prefs(ctx);
        String salvo = prefs.getString("alert_marcos", null);
        if (salvo == null || salvo.trim().isEmpty()) return MARCOS_PADRAO.clone();

        List<Integer> lista = new ArrayList<>();
        for (String parte : salvo.split(",")) {
            try {
                int v = Integer.parseInt(parte.trim());
                if (v > 0 && !lista.contains(v)) lista.add(v);
            } catch (NumberFormatException ignored) {}
        }
        if (lista.isEmpty()) return MARCOS_PADRAO.clone();
        Collections.sort(lista, Collections.reverseOrder());
        int[] arr = new int[lista.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = lista.get(i);
        return arr;
    }

    public static void setMarcos(Context ctx, List<Integer> valores) {
        List<Integer> copia = new ArrayList<>(valores);
        Collections.sort(copia, Collections.reverseOrder());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < copia.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(copia.get(i));
        }
        prefs(ctx).edit().putString("alert_marcos", sb.toString()).apply();
        BackupHelper.backupNow(ctx);
    }

    public static long getIntervaloBlocker(Context ctx) {
        long v = prefs(ctx).getLong("intervalo_blocker_ms", BLOCKER_PADRAO_MS);
        return Math.max(v, MIN_INTERVALO_MS);
    }

    public static void setIntervaloBlocker(Context ctx, long ms) {
        long v = Math.max(ms, MIN_INTERVALO_MS);
        prefs(ctx).edit().putLong("intervalo_blocker_ms", v).apply();
        BackupHelper.backupNow(ctx);
    }

    public static long getIntervaloWatchdog(Context ctx) {
        long v = prefs(ctx).getLong("intervalo_watchdog_ms", WATCHDOG_PADRAO_MS);
        return Math.max(v, MIN_INTERVALO_MS);
    }

    public static void setIntervaloWatchdog(Context ctx, long ms) {
        long v = Math.max(ms, MIN_INTERVALO_MS);
        prefs(ctx).edit().putLong("intervalo_watchdog_ms", v).apply();
        BackupHelper.backupNow(ctx);
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }
}
