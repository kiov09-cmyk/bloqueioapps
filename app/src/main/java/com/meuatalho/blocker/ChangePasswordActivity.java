package com.meuatalho.blocker;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePasswordActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences prefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(64, 64, 64, 64);

        TextView title = new TextView(this);
        title.setText("Trocar senha");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 32);
        root.addView(title);

        EditText atual = campo(root, "Senha atual");
        EditText nova = campo(root, "Nova senha");
        EditText confirma = campo(root, "Confirme a nova senha");

        Button salvar = new Button(this);
        salvar.setText("Salvar");
        salvar.setTextSize(18);
        salvar.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 32;
        salvar.setLayoutParams(lp);
        salvar.setOnClickListener(v -> {
            String a = atual.getText().toString();
            String n = nova.getText().toString();
            String c = confirma.getText().toString();
            String senhaSalva = prefs.getString("password", "");
            if (!a.equals(senhaSalva)) {
                Toast.makeText(this, "Senha atual incorreta", Toast.LENGTH_SHORT).show();
                return;
            }
            if (n.length() < 4) {
                Toast.makeText(this, "Nova senha: mínimo 4 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!n.equals(c)) {
                Toast.makeText(this, "As novas senhas não coincidem", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString("password", n).apply();
            Toast.makeText(this, "Senha alterada", Toast.LENGTH_SHORT).show();
            finish();
        });
        root.addView(salvar);

        setContentView(root);
        atual.requestFocus();
    }

    private EditText campo(LinearLayout root, String hint) {
        EditText e = new EditText(this);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        e.setHint(hint);
        e.setFocusable(true);
        e.setFocusableInTouchMode(true);
        e.setTextSize(20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 12;
        e.setLayoutParams(lp);
        root.addView(e);
        return e;
    }
}
