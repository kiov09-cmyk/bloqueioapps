package com.meuatalho.blocker;

import android.content.Context;
import android.provider.Settings;

public class AccessibilityHelper {
    public static final String SERVICE = "com.meuatalho.blocker/com.meuatalho.blocker.BlockerService";

    public static void ativar(Context ctx) {
        try {
            Settings.Secure.putInt(ctx.getContentResolver(), "accessibility_enabled", 1);
            String atual = Settings.Secure.getString(ctx.getContentResolver(), "enabled_accessibility_services");
            if (atual == null || atual.isEmpty()) {
                Settings.Secure.putString(ctx.getContentResolver(), "enabled_accessibility_services", SERVICE);
            } else if (!atual.contains(SERVICE)) {
                Settings.Secure.putString(ctx.getContentResolver(), "enabled_accessibility_services", atual + ":" + SERVICE);
            }
        } catch (SecurityException ignored) {
            // permissao WRITE_SECURE_SETTINGS ainda nao concedida
        }
    }
}
