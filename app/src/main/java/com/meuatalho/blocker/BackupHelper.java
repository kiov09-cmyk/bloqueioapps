package com.meuatalho.blocker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackupHelper {

    private static File backupFile() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "BloqueioApps");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "backup.cfg");
    }

    public static void backupNow(Context ctx) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
            Map<String, ?> all = prefs.getAll();
            FileWriter fw = new FileWriter(backupFile());
            for (Map.Entry<String, ?> e : all.entrySet()) {
                Object v = e.getValue();
                if (v instanceof Set) {
                    StringBuilder sb = new StringBuilder();
                    for (Object item : (Set<?>) v) sb.append(item).append("\u0001");
                    fw.write("SET:" + e.getKey() + "=" + sb + "\n");
                } else {
                    fw.write("VAL:" + e.getKey() + "=" + v + "\n");
                }
            }
            fw.close();
        } catch (Exception ignored) {}
    }

    public static boolean restoreIfNeeded(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE);
        if (prefs.contains("password")) return false;
        File f = backupFile();
        if (!f.exists()) return false;
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            SharedPreferences.Editor ed = prefs.edit();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("SET:")) {
                    int eq = line.indexOf('=');
                    String key = line.substring(4, eq);
                    String[] parts = line.substring(eq + 1).split("\u0001");
                    Set<String> set = new HashSet<>();
                    for (String p : parts) if (!p.isEmpty()) set.add(p);
                    ed.putStringSet(key, set);
                } else if (line.startsWith("VAL:")) {
                    int eq = line.indexOf('=');
                    String key = line.substring(4, eq);
                    String val = line.substring(eq + 1);
                    if (key.equals("password")) ed.putString(key, val);
                    else if (key.startsWith("unlock_")) { try { ed.putLong(key, Long.parseLong(val)); } catch (Exception ignored) {} }
                    else if (key.startsWith("minutes_")) { try { ed.putInt(key, Integer.parseInt(val)); } catch (Exception ignored) {} }
                    else ed.putString(key, val);
                }
            }
            br.close();
            ed.apply();
            return true;
        } catch (Exception e) { return false; }
    }
}
