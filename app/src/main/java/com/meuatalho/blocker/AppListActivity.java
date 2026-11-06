package com.meuatalho.blocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.media.RingtoneManager;
import android.net.Uri;
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
import android.widget.SeekBar;
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
    private String homePkg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);

        PackageManager pm = getPackageManager();
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        ResolveInfo ri = pm.resolveActivity(home, 0);
        homePkg = (ri != null && ri.activityInfo != null) ? ri.activityInfo.packageName : null;

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

        Button somBtn = botaoGrande("Escolher som");
        LinearLayout.LayoutParams lpSom = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpSom.leftMargin = 16;
        somBtn.setLayoutParams(lpSom);
        somBtn.setOnClickListener(v -> {
            Intent picker = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            String atual = prefs.getString("alert_sound_uri", null);
            if (atual != null) picker.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(atual));
            startActivityForResult(picker, 100);
        });
        topo.addView(somBtn);

        root.addView(topo);

        LinearLayout volRow = new LinearLayout(this);
        volRow.setOrientation(LinearLayout.HORIZONTAL);
        volRow.setGravity(Gravity.CENTER_VERTICAL);
        volRow.setPadding(0, 16, 0, 0);
        TextView volLabel = new TextView(this);
        int volInicial = prefs.getInt("alert_volume", 50);
        volLabel.setText("Volume do alerta: " + volInicial + "%");
        volLabel.setTextSize(14);
        volRow.addView(volLabel);
        SeekBar seek = new SeekBar(this);
        seek.setMax(100);
        seek.setProgress(volInicial);
        LinearLayout.LayoutParams lpSeek = new LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpSeek.leftMargin = 16;
        seek.setLayoutParams(lpSeek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                volLabel.setText("Volume do alerta: " + progress + "%");
                prefs.edit().putInt("alert_volume", progress).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) { BackupHelper.backupNow(AppListActivity.this); }
        });
        volRow.addView(seek);
        root.addView(volRow);

        TextView dica = new TextView(this);
        dica.setText("Ative \"Bloqueio de Apps\" na lista que abrir. Depois marque abaixo os apps a bloquear.\n(O launcher e as Configurações não aparecem aqui, por segurança.)");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                prefs.edit().putString("alert_sound_uri", uri.toString()).apply();
                Toast.makeText(this, "Som de alerta definido", Toast.LENGTH_SHORT).show();
            } else {
                prefs.edit().remove("alert_sound_uri").apply();
                Toast.makeText(this, "Alerta sonoro desativado (silencioso)", Toast.LENGTH_SHORT).show();
            }
            BackupHelper.backupNow(this);
        }
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

        for (ResolveInfo ri : apps) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            if (pkg.equals(homePkg)) continue;
            if (pkg.equals("com.android.settings") || pkg.equals("com.android.tv.settings")) continue;
            if (ehLauncher(pm, pkg)) continue;

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

            Button liberar = botaoGrande("Liberar e abrir");
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
                BackupHelper.backupNow(this);
                Toast.makeText(this, label + " liberado por " + min + " min", Toast.LENGTH_SHORT).show();
                Intent launch = pm.getLaunchIntentForPackage(pkg);
                if (launch != null) startActivity(launch);
            });

            cb.setOnCheckedChangeListener((btn, checked) -> {
                Set<String> atual = new HashSet<>(prefs.getStringSet("blocked_apps", new HashSet<>()));
                if (checked) atual.add(pkg); else atual.remove(pkg);
                prefs.edit().putStringSet("blocked_apps", atual).apply();
                BackupHelper.backupNow(this);
                sub.setVisibility(checked ? View.VISIBLE : View.GONE);
            });

            row.addView(sub);
            list.addView(row);
        }
    }

    private boolean ehLauncher(PackageManager pm, String pkg) {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> launchers = pm.queryIntentActivities(home, 0);
        for (ResolveInfo l : launchers) {
            if (l.activityInfo.packageName.equals(pkg)) return true;
        }
        return false;
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
