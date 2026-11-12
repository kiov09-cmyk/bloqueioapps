package com.meuatalho.blocker;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class RefreshActivity extends Activity {

    private EditText campoBlocker, campoWatchdog;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Intervalos de verificação (refresh)");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        TextView dica = new TextView(this);
        dica.setText("Define de quanto em quanto tempo o app confere se deve bloquear (Bloqueio) e se a acessibilidade continua ativa (Watchdog). Mínimo de " + (ConfigHelper.MIN_INTERVALO_MS / 1000) + " segundo(s) em cada um — valores menores são ajustados automaticamente.");
        dica.setTextSize(14);
        dica.setPadding(0, 0, 0, 24);
        root.addView(dica);

        TextView labelBlocker = new TextView(this);
        labelBlocker.setText("Verificação de bloqueio (segundos):");
        labelBlocker.setTextSize(16);
        root.addView(labelBlocker);

        campoBlocker = new EditText(this);
        campoBlocker.setInputType(InputType.TYPE_CLASS_NUMBER);
        campoBlocker.setText(String.valueOf(ConfigHelper.getIntervaloBlocker(this) / 1000));
        campoBlocker.setTextSize(18);
        campoBlocker.setFocusable(true);
        campoBlocker.setFocusableInTouchMode(true);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp1.topMargin = 8;
        lp1.bottomMargin = 24;
        campoBlocker.setLayoutParams(lp1);
        root.addView(campoBlocker);

        TextView labelWatchdog = new TextView(this);
        labelWatchdog.setText("Verificação do Watchdog (segundos):");
        labelWatchdog.setTextSize(16);
        root.addView(labelWatchdog);

        campoWatchdog = new EditText(this);
        campoWatchdog.setInputType(InputType.TYPE_CLASS_NUMBER);
        campoWatchdog.setText(String.valueOf(ConfigHelper.getIntervaloWatchdog(this) / 1000));
        campoWatchdog.setTextSize(18);
        campoWatchdog.setFocusable(true);
        campoWatchdog.setFocusableInTouchMode(true);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp2.topMargin = 8;
        campoWatchdog.setLayoutParams(lp2);
        root.addView(campoWatchdog);

        LinearLayout botoes = new LinearLayout(this);
        botoes.setOrientation(LinearLayout.HORIZONTAL);
        botoes.setPadding(0, 32, 0, 0);

        Button salvar = new Button(this);
        salvar.setText("Salvar");
        salvar.setFocusable(true);
        salvar.setOnClickListener(v -> salvar());
        botoes.addView(salvar);

        Button voltar = new Button(this);
        voltar.setText("Voltar");
        voltar.setFocusable(true);
        LinearLayout.LayoutParams lpVoltar = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpVoltar.leftMargin = 16;
        voltar.setLayoutParams(lpVoltar);
        voltar.setOnClickListener(v -> finish());
        botoes.addView(voltar);

        root.addView(botoes);

        setContentView(scroll);
    }

    private void salvar() {
        long blockerMs = parseSegundos(campoBlocker.getText().toString());
        long watchdogMs = parseSegundos(campoWatchdog.getText().toString());
        if (blockerMs < 0 || watchdogMs < 0) {
            Toast.makeText(this, "Digite valores numéricos válidos", Toast.LENGTH_SHORT).show();
            return;
        }
        ConfigHelper.setIntervaloBlocker(this, blockerMs);
        ConfigHelper.setIntervaloWatchdog(this, watchdogMs);
        Toast.makeText(this, "Intervalos salvos (mínimo de " + (ConfigHelper.MIN_INTERVALO_MS / 1000) + "s aplicado automaticamente)", Toast.LENGTH_LONG).show();
        finish();
    }

    private long parseSegundos(String texto) {
        try {
            long segundos = Long.parseLong(texto.trim());
            return segundos * 1000L;
        } catch (Exception e) {
            return -1;
        }
    }
}
