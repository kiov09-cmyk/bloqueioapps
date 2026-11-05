package com.meuatalho.blocker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final String PREFS = "blocker_prefs";
    private EditText input, confirm;
    private boolean hasPassword;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        BackupHelper.restoreIfNeeded(this);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        hasPassword = prefs.contains("password");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);

        TextView title = new TextView(this);
        title.setText(hasPassword ? "Digite a senha" : "Crie uma senha (mín. 4 dígitos)");
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setHint("Senha");
        input.setFocusable(true);
        input.setFocusableInTouchMode(true);
        input.setTextSize(20);
        root.addView(input);

        if (!hasPassword) {
            confirm = new EditText(this);
            confirm.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            confirm.setHint("Confirme a senha");
            confirm.setFocusable(true);
            confirm.setFocusableInTouchMode(true);
            confirm.setTextSize(20);
            confirm.setPadding(0, 16, 0, 0);
            root.addView(confirm);
        }

        Button go = new Button(this);
        go.setText(hasPassword ? "Entrar" : "Criar e entrar");
        go.setFocusable(true);
        go.setTextSize(18);
        go.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 32;
        go.setLayoutParams(lp);
        go.setOnClickListener(v -> tentar());
        root.addView(go);

        setContentView(root);
        input.requestFocus();
    }

    private void tentar() {
        String val = input.getText().toString();
        if (val.length() < 4) {
            Toast.makeText(this, "Mínimo 4 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!hasPassword) {
            String conf = confirm.getText().toString();
            if (!val.equals(conf)) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }
            p.edit().putString("password", val).apply();
            BackupHelper.backupNow(this);
            abrirLista();
        } else if (p.getString("password", "").equals(val)) {
            abrirLista();
        } else {
            Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirLista() {
        startActivity(new Intent(this, AppListActivity.class));
        finish();
    }
}
