package com.escondidinho.comandainteligente;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.LinkedHashSet;
import java.util.Set;

public class OrderAccessibilityService extends AccessibilityService {
    private long lastSave = 0L;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        long now = System.currentTimeMillis();
        if (now - lastSave < 700) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;

        Set<String> lines = new LinkedHashSet<>();
        collectText(root, lines);
        if (lines.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String cleaned = line.trim();
            if (!cleaned.isEmpty()) sb.append(cleaned).append('\n');
        }

        String text = sb.toString().trim();
        if (!looksLikeOrder(text)) return;

        CharSequence pkgCs = event.getPackageName();
        String pkg = pkgCs == null ? "" : pkgCs.toString();

        SharedPreferences prefs = getSharedPreferences("comanda", MODE_PRIVATE);
        prefs.edit()
                .putString("last_capture", text)
                .putString("last_package", pkg)
                .putLong("last_time", now)
                .apply();
        lastSave = now;
    }

    private void collectText(AccessibilityNodeInfo node, Set<String> lines) {
        if (node == null) return;

        CharSequence text = node.getText();
        if (text != null) addMultiline(lines, text.toString());

        CharSequence description = node.getContentDescription();
        if (description != null) addMultiline(lines, description.toString());

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                collectText(child, lines);
                child.recycle();
            }
        }
    }

    private void addMultiline(Set<String> lines, String value) {
        for (String part : value.split("\\r?\\n")) {
            String s = part.trim();
            if (!s.isEmpty() && s.length() <= 500) lines.add(s);
        }
    }

    private boolean looksLikeOrder(String text) {
        String t = text.toLowerCase();
        boolean hasOrderWord = t.contains("pedido") || t.contains("itens") || t.contains("total dos itens");
        boolean hasMoney = t.contains("r$") || t.contains("total");
        boolean hasQuantity = t.matches("(?s).*(^|\\n)\\s*[1-9]\\d*\\s*[xX].*") || t.contains("1x") || t.contains("2x");
        return hasOrderWord && (hasMoney || hasQuantity);
    }

    @Override
    public void onInterrupt() {
    }
}
