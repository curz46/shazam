package com.github.shazam.util;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    public static String getServerVersion() {
        final String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }

}
