package com.meuatalho.blocker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AutostartActivity extends Activity {

    private static final Intent[] TENTATIVAS = {
        intentFabricante("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        intentFabricante("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivityNew"),
        intentFabricante("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
        intentFabricante("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
        intentFabricante("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
        intentFabricante("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        intentFabricante("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
        intentFabricante("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
        intentFabricante("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"),
        intentFabricante("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity"),
        intentFabricante("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Permitir início automático");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        TextView dica = new TextView(this);
        dica.setText("Alguns fabricantes (Xiaomi, Huawei, Samsung, entre outros) bloqueiam apps em segundo plano por padrão, mesmo com o serviço de acessibilidade ativo. Toque no botão abaixo para tentar abrir a tela de gerenciamento do fabricante e liberar o início automático do Bloqueio de Apps. Se não abrir nada específico, vai cair na tela padrão de otimização de bateria do Android.");
        dica.setTextSize(14);
        dica.setPadding(0, 0, 0, 24);
        root.addView(dica);

        Button abrirBtn = new Button(this);
        abrirBtn.setText("Abrir configurações do fabricante");
        abrirBtn.setFocusable(true);
        abrirBtn.setTextSize(16);
        abrirBtn.setPadding(24, 20, 24, 20);
        abrirBtn.setOnClickListener(v -> tentarAbrir());
        root.addView(abrirBtn);

        Button genericoBtn = new Button(this);
        genericoBtn.setText("Abrir otimização de bateria (padrão Android)");
        genericoBtn.setFocusable(true);
        genericoBtn.setTextSize(16);
        genericoBtn.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lpGen = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpGen.topMargin = 16;
        genericoBtn.setLayoutParams(lpGen);
        genericoBtn.setOnClickListener(v -> abrirGenerico());
        root.addView(genericoBtn);

        Button voltar = new Button(this);
        voltar.setText("Voltar");
        voltar.setTextSize(16);
        voltar.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lpVoltar = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpVoltar.topMargin = 24;
        voltar.setLayoutParams(lpVoltar);
        voltar.setFocusable(true);
        voltar.setOnClickListener(v -> finish());
        root.addView(voltar);

        setContentView(scroll);
        abrirBtn.requestFocus();
    }

    private void tentarAbrir() {
        for (Intent intent : TENTATIVAS) {
            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException | SecurityException ignored) {
                // tenta a proxima
            }
        }
        Toast.makeText(this, "Nenhuma tela específica encontrada, abrindo a padrão do Android", Toast.LENGTH_LONG).show();
        abrirGenerico();
    }

    private void abrirGenerico() {
        try {
            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS));
            } catch (Exception ignored2) {
                Toast.makeText(this, "Não foi possível abrir nenhuma tela de configuração", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static Intent intentFabricante(String pacote, String classe) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(pacote, classe));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
