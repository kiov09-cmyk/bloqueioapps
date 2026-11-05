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
    private EditText input;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean hasPassword = prefs.contains("password");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);

        TextView title = new TextView(this);
        title.setText(hasPassword ? "Digite a senha" : "Crie uma senha (mín. 4 dígitos)");
        title.setTextSize(20);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Senha");
        root.addView(input);

        Button go = new Button(this);
        go.setText(hasPassword ? "Entrar" : "Criar e entrar");
        go.setOnClickListener(v -> {
            String val = input.getText().toString();
            if (val.length() < 4) {
                Toast.makeText(this, "Mínimo 4 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
            if (!p.contains("password")) {
                p.edit().putString("password", val).apply();
                abrirLista();
            } else if (p.getString("password", "").equals(val)) {
                abrirLista();
            } else {
                Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(go);

        setContentView(root);
    }

    private void abrirLista() {
        startActivity(new Intent(this, AppListActivity.class));
        finish();
    }
}
