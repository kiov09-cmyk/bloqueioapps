package com.meuatalho.blocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AppListActivity extends Activity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 32);

        LinearLayout topo = new LinearLayout(this);
        topo.setOrientation(LinearLayout.HORIZONTAL);

        Button acc = botaoGrande("Ativar acessibilidade");
        acc.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        topo.addView(acc);

        Button trocarSenha = botaoGrande("Trocar senha");
        LinearLayout.LayoutParams lpTs = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTs.leftMargin = 16;
        trocarSenha.setLayoutParams(lpTs);
        trocarSenha.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));
        topo.addView(trocarSenha);

        root.addView(topo);

        TextView dica = new TextView(this);
        dica.setText("Ative \"Bloqueio de Apps\" na lista que abrir. Depois marque abaixo os apps a bloquear.");
        dica.setTextSize(14);
        dica.setPadding(0, 16, 0, 24);
        root.addView(dica);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);
        popularLista(list);
        acc.requestFocus();
    }

    private Button botaoGrande(String texto) {
        Button btn = new Button(this);
        btn.setText(texto);
        btn.setTextSize(16);
        btn.setPadding(24, 20, 24, 20);
        btn.setFocusable(true);
        return btn;
    }

    private void popularLista(LinearLayout list) {
        PackageManager pm = getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        apps.sort((a, c) -> a.loadLabel(pm).toString().compareToIgnoreCase(c.loadLabel(pm).toString()));

        Set<String> blocked = new HashSet<>(prefs.getStringSet("blocked_apps", new HashSet<>()));
        Button anterior = null;

        for (ResolveInfo ri : apps) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            String label = ri.loadLabel(pm).toString();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 12, 0, 12);

            CheckBox cb = new CheckBox(this);
            cb.setText(label);
            cb.setTextSize(17);
            cb.setFocusable(true);
            cb.setChecked(blocked.contains(pkg));
            row.addView(cb);

            LinearLayout sub = new LinearLayout(this);
            sub.setOrientation(LinearLayout.HORIZONTAL);
            sub.setGravity(Gravity.CENTER_VERTICAL);
            sub.setPadding(48, 8, 0, 4);
            sub.setVisibility(cb.isChecked() ? View.VISIBLE : View.GONE);

            int minutosSalvos = prefs.getInt("minutes_" + pkg, 10);

            EditText minutos = new EditText(this);
            minutos.setInputType(InputType.TYPE_CLASS_NUMBER);
            minutos.setHint("min");
            minutos.setText(String.valueOf(minutosSalvos));
            minutos.setTextSize(17);
            minutos.setFocusable(true);
            minutos.setFocusableInTouchMode(true);
            minutos.setLayoutParams(new LinearLayout.LayoutParams(160, ViewGroup.LayoutParams.WRAP_CONTENT));
            sub.addView(minutos);

            Button liberar = botaoGrande("Liberar agora");
            LinearLayout.LayoutParams lpLib = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lpLib.leftMargin = 16;
            liberar.setLayoutParams(lpLib);
            sub.addView(liberar);

            TextView status = new TextView(this);
            status.setTextSize(15);
            status.setPadding(24, 0, 0, 0);
            atualizarStatus(status, pkg);
            sub.addView(status);

            liberar.setOnClickListener(v -> {
                int min = 10;
                try { min = Integer.parseInt(minutos.getText().toString()); } catch (Exception ignored) {}
                prefs.edit().putInt("minutes_" + pkg, min).apply();
                long ate = System.currentTimeMillis() + min * 60000L;
                prefs.edit().putLong("unlock_" + pkg, ate).apply();
                atualizarStatus(status, pkg);
                Toast.makeText(this, label + " liberado por " + min + " min", Toast.LENGTH_SHORT).show();
            });

            cb.setOnCheckedChangeListener((btn, checked) -> {
                Set<String> atual = new HashSet<>(prefs.getStringSet("blocked_apps", new HashSet<>()));
                if (checked) atual.add(pkg); else atual.remove(pkg);
                prefs.edit().putStringSet("blocked_apps", atual).apply();
                sub.setVisibility(checked ? View.VISIBLE : View.GONE);
            });

            row.addView(sub);
            list.addView(row);

            if (anterior != null) {
                anterior.setNextFocusDownId(cb.hashCode());
            }
            cb.setId(View.generateViewId());
            anterior = liberar;
        }
    }

    private void atualizarStatus(TextView status, String pkg) {
        long ate = prefs.getLong("unlock_" + pkg, 0);
        if (ate > System.currentTimeMillis()) {
            String h = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ate));
            status.setText("Liberado até " + h);
        } else {
            status.setText("Bloqueado");
        }
    }
}
