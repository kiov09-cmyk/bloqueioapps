package com.meuatalho.blocker;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AlertasActivity extends Activity {

    private LinearLayout listaLayout;
    private final List<EditText> campos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Marcos de alerta (minutos restantes)");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 16);
        root.addView(title);

        TextView dica = new TextView(this);
        dica.setText("Defina em quantos minutos antes de bloquear o app o aviso sonoro/na tela deve aparecer. Pode adicionar quantos marcos quiser.");
        dica.setTextSize(14);
        dica.setPadding(0, 0, 0, 24);
        root.addView(dica);

        listaLayout = new LinearLayout(this);
        listaLayout.setOrientation(LinearLayout.VERTICAL);
        root.addView(listaLayout);

        for (int m : ConfigHelper.getMarcos(this)) {
            adicionarLinha(String.valueOf(m));
        }

        Button addBtn = new Button(this);
        addBtn.setText("+ Adicionar marco");
        addBtn.setFocusable(true);
        addBtn.setOnClickListener(v -> adicionarLinha(""));
        LinearLayout.LayoutParams lpAdd = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpAdd.topMargin = 16;
        addBtn.setLayoutParams(lpAdd);
        root.addView(addBtn);

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

    private void adicionarLinha(String valorInicial) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 8, 0, 8);

        EditText campo = new EditText(this);
        campo.setInputType(InputType.TYPE_CLASS_NUMBER);
        campo.setHint("min");
        campo.setText(valorInicial);
        campo.setTextSize(17);
        campo.setFocusable(true);
        campo.setFocusableInTouchMode(true);
        campo.setLayoutParams(new LinearLayout.LayoutParams(160, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(campo);
        campos.add(campo);

        Button remover = new Button(this);
        remover.setText("Remover");
        remover.setFocusable(true);
        LinearLayout.LayoutParams lpRem = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpRem.leftMargin = 16;
        remover.setLayoutParams(lpRem);
        remover.setOnClickListener(v -> {
            listaLayout.removeView(row);
            campos.remove(campo);
        });
        row.addView(remover);

        listaLayout.addView(row);
    }

    private void salvar() {
        List<Integer> valores = new ArrayList<>();
        for (EditText campo : campos) {
            String texto = campo.getText().toString().trim();
            if (texto.isEmpty()) continue;
            try {
                int v = Integer.parseInt(texto);
                if (v > 0 && !valores.contains(v)) valores.add(v);
            } catch (NumberFormatException ignored) {}
        }
        if (valores.isEmpty()) {
            Toast.makeText(this, "Defina pelo menos um marco válido", Toast.LENGTH_SHORT).show();
            return;
        }
        ConfigHelper.setMarcos(this, valores);
        Toast.makeText(this, "Marcos salvos", Toast.LENGTH_SHORT).show();
        finish();
    }
}
