package com.escondidinho.comandainteligente;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private TextView capture;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("comanda", MODE_PRIVATE);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 42, 36, 42);
        root.setBackgroundColor(Color.rgb(250, 247, 243));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("COMANDA INTELIGENTE");
        title.setTextSize(26);
        title.setTextColor(Color.rgb(46, 26, 18));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 18);
        root.addView(title);

        TextView version = new TextView(this);
        version.setText("v0.1 • Diagnóstico de captura");
        version.setTextSize(14);
        version.setGravity(Gravity.CENTER);
        version.setPadding(0, 0, 0, 28);
        root.addView(version);

        status = new TextView(this);
        status.setTextSize(16);
        status.setPadding(18, 18, 18, 18);
        root.addView(status);

        Button access = new Button(this);
        access.setText("ATIVAR ACESSIBILIDADE");
        access.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(access);

        Button refresh = new Button(this);
        refresh.setText("ATUALIZAR ÚLTIMA CAPTURA");
        refresh.setOnClickListener(v -> refreshCapture());
        root.addView(refresh);

        Button clear = new Button(this);
        clear.setText("LIMPAR CAPTURA");
        clear.setOnClickListener(v -> {
            prefs.edit().remove("last_capture").remove("last_package").remove("last_time").apply();
            refreshCapture();
        });
        root.addView(clear);

        TextView label = new TextView(this);
        label.setText("\nÚLTIMA CAPTURA");
        label.setTextSize(18);
        label.setTextColor(Color.rgb(46, 26, 18));
        label.setPadding(0, 12, 0, 10);
        root.addView(label);

        capture = new TextView(this);
        capture.setTextSize(15);
        capture.setTextColor(Color.BLACK);
        capture.setTextIsSelectable(true);
        capture.setPadding(20, 20, 20, 20);
        capture.setBackgroundColor(Color.WHITE);
        root.addView(capture);

        TextView note = new TextView(this);
        note.setText("\nComo testar:\n1. Ative a acessibilidade da Comanda Inteligente.\n2. Abra o Portal e entre em um pedido do histórico.\n3. Role a tela do pedido.\n4. Volte aqui e toque em Atualizar.\n\nPrivacidade: esta versão não possui permissão de internet e só salva localmente a última tela que pareça conter um pedido.");
        note.setTextSize(14);
        note.setPadding(0, 20, 0, 20);
        root.addView(note);

        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCapture();
    }

    private void refreshCapture() {
        boolean enabled = isAccessibilityServiceEnabled();
        status.setText(enabled ? "Status: MONITORAMENTO ATIVO" : "Status: ACESSIBILIDADE DESATIVADA");
        status.setTextColor(enabled ? Color.rgb(0, 110, 45) : Color.rgb(180, 40, 40));

        String text = prefs.getString("last_capture", "Nenhum pedido capturado ainda.");
        String pkg = prefs.getString("last_package", "");
        long time = prefs.getLong("last_time", 0L);
        String meta = "";
        if (!pkg.isEmpty()) meta += "App: " + pkg + "\n";
        if (time > 0) meta += "Capturado: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(time)) + "\n\n";
        capture.setText(meta + text);
    }

    private boolean isAccessibilityServiceEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null) return false;
        String expected = getPackageName() + "/" + OrderAccessibilityService.class.getName();
        return enabled.toLowerCase().contains(expected.toLowerCase());
    }
}
