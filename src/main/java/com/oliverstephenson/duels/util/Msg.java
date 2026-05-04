package com.oliverstephenson.duels.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Msg {

    private static final Component PREFIX =
            Component.text("[Duels] ", NamedTextColor.AQUA);

    private Msg() {}

    public static Component info(String text) {
        return PREFIX.append(Component.text(text, NamedTextColor.WHITE));
    }

    public static Component warn(String text) {
        return PREFIX.append(Component.text(text, NamedTextColor.YELLOW));
    }

    public static Component err(String text) {
        return PREFIX.append(Component.text(text, NamedTextColor.RED));
    }

    public static Component success(String text) {
        return PREFIX.append(Component.text(text, NamedTextColor.GREEN));
    }
}