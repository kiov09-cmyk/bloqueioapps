package com.meuatalho.blocker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Sobre o Bloqueio de Apps");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 20);
        root.addView(title);

        root.addView(paragrafo(
            "O que o app faz:\n" +
            "- Protege a entrada com senha.\n" +
            "- Permite escolher quais apps ficam bloqueados.\n" +
            "- Libera cada app por um tempo definido em minutos.\n" +
            "- Mostra avisos na tela e som quando o tempo está acabando, em marcos configuráveis (botão Alertas).\n" +
            "- Fecha o app sozinho quando o tempo liberado expira, usando um serviço de acessibilidade.\n" +
            "- Tem um monitor (Watchdog) que confere periodicamente se a proteção continua ativa; se não estiver, zera as liberações em andamento e volta para a tela inicial, em vez de deixar o app liberado sem supervisão.\n" +
            "- O intervalo dessas verificações pode ser ajustado no botão Refresh (mínimo de 5 segundos)."
        ));

        root.addView(paragrafo(
            "Proteção do launcher e das Configurações:\n" +
            "O launcher atual da TV (e qualquer outro app que ofereça tela inicial) e os apps de Configurações do sistema (com.android.settings e com.android.tv.settings) nunca aparecem na lista de apps que podem ser bloqueados. Essa exclusão é fixa no código do app, não é uma opção que possa ser desmarcada ou burlada pela tela — então não tem como bloquear o launcher ou as Configurações por engano."
        ));

        root.addView(paragrafo(
            "Cuidado a ter:\n" +
            "Se a sua TV box usar um app de Configurações com um nome de pacote diferente dos dois acima (algumas marcas customizam isso), essa exclusão específica pode não cobrir esse caso. Se isso acontecer e as Configurações forem bloqueadas por engano, desinstale o app (ou use os comandos da tela Root+ para reverter, se instalado como app de sistema) para liberar o acesso."
        ));

        root.addView(paragrafo(
            "Sem root, o app funciona normalmente, mas pode ser desinstalado como qualquer outro e, depois de um reboot, pode pedir para reativar a acessibilidade manualmente (o próprio app guia esse passo ao tentar liberar um app). Com root, instalado como app de sistema (tela Root+), ganha resistência à desinstalação e reativação automática da acessibilidade no boot."
        ));

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

    private TextView paragrafo(String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextSize(15);
        tv.setPadding(0, 0, 0, 20);
        return tv;
    }
}
