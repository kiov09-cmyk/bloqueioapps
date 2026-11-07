package com.meuatalho.blocker;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class InfoActivity extends Activity {

    private static final String COMANDO_REATIVAR =
            "adb root\n" +
            "adb shell settings put secure enabled_accessibility_services com.meuatalho.blocker/com.meuatalho.blocker.BlockerService\n" +
            "adb shell settings put secure accessibility_enabled 1";

    private static final String COMANDO_VERIFICAR =
            "adb shell settings get secure enabled_accessibility_services\n" +
            "adb shell ps | grep meuatalho";

    private static final String COMANDO_INSTALAR =
            "adb root\n" +
            "adb remount\n" +
            "adb shell mkdir -p /system/app/BloqueioApps\n" +
            "adb push app-debug.apk /system/app/BloqueioApps/BloqueioApps.apk\n" +
            "adb shell chmod 644 /system/app/BloqueioApps/BloqueioApps.apk\n" +
            "adb shell pm uninstall --user 0 com.meuatalho.blocker\n" +
            "adb reboot\n" +
            "# depois do boot:\n" +
            "adb shell pm grant com.meuatalho.blocker android.permission.WRITE_SECURE_SETTINGS\n" +
            "adb shell settings put secure enabled_accessibility_services com.meuatalho.blocker/com.meuatalho.blocker.BlockerService\n" +
            "adb shell settings put secure accessibility_enabled 1";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Informações e comandos úteis");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 24);
        root.addView(title);

        TextView aviso = new TextView(this);
        aviso.setText("Toda vez que o app for reinstalado ou atualizado, o Android desativa " +
                "automaticamente o serviço de acessibilidade por segurança (isso é do sistema, " +
                "não dá para evitar). Rode o comando abaixo no computador/Termux para reativar sem " +
                "precisar mexer na tela da TV.");
        aviso.setTextSize(15);
        aviso.setPadding(0, 0, 0, 20);
        root.addView(aviso);

        root.addView(blocoComando("Reativar acessibilidade", COMANDO_REATIVAR));
        root.addView(blocoComando("Verificar status", COMANDO_VERIFICAR));
        root.addView(blocoComando("Instalar como app de sistema (novo APK)", COMANDO_INSTALAR));

        Button voltar = new Button(this);
        voltar.setText("Voltar");
        voltar.setTextSize(16);
        voltar.setPadding(24, 20, 24, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 24;
        voltar.setLayoutParams(lp);
        voltar.setFocusable(true);
        voltar.setOnClickListener(v -> finish());
        root.addView(voltar);

        setContentView(scroll);
        voltar.requestFocus();
    }

    private LinearLayout blocoComando(String titulo, String comando) {
        LinearLayout bloco = new LinearLayout(this);
        bloco.setOrientation(LinearLayout.VERTICAL);
        bloco.setPadding(0, 12, 0, 12);

        TextView label = new TextView(this);
        label.setText(titulo);
        label.setTextSize(17);
        bloco.addView(label);

        TextView texto = new TextView(this);
        texto.setText(comando);
        texto.setTextSize(14);
        texto.setTypeface(android.graphics.Typeface.MONOSPACE);
        texto.setTextIsSelectable(true);
        texto.setBackgroundColor(0xFF1b2436);
        texto.setTextColor(0xFFe6edf3);
        texto.setPadding(24, 16, 24, 16);
        LinearLayout.LayoutParams lpTexto = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpTexto.topMargin = 8;
        texto.setLayoutParams(lpTexto);
        bloco.addView(texto);

        Button copiar = new Button(this);
        copiar.setText("Copiar comando");
        copiar.setFocusable(true);
        LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpBtn.topMargin = 8;
        copiar.setLayoutParams(lpBtn);
        copiar.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("comando", comando));
            Toast.makeText(this, "Comando copiado", Toast.LENGTH_SHORT).show();
        });
        bloco.addView(copiar);

        return bloco;
    }
}
