package com.github.shazam.util;

import org.bukkit.util.Vector;

public class VectorUtil {

    public static Vector rotate(Vector initial, float yaw, float pitch) {
        final double yawR = yaw / 180.0 * Math.PI;
        final double pitchR = pitch / 180.0 * Math.PI;
        return rotateAboutY(rotateAboutX(initial, pitchR), -yawR);
    }

    private static Vector rotateAboutX(Vector initial, double a) {
        final double y = Math.cos(a) * initial.getY() - Math.sin(a) * initial.getZ();
        final double z = Math.sin(a) * initial.getY() + Math.cos(a) * initial.getZ();
        return initial.setY(y).setZ(z);
    }

    private static Vector rotateAboutY(Vector initial, double a) {
        final double x = Math.cos(a) * initial.getX() + Math.sin(a) * initial.getZ();
        final double z = -Math.sin(a) * initial.getX() + Math.cos(a) * initial.getZ();
        return initial.setX(x).setZ(z);
    }

//    private Vector rotateAboutZ(Vector initial, double a) {
//        final double x = Math.cos(a) * initial.getX() - Math.sin(a) * initial.getY();
//        final double y = Math.sin(a) * initial.getX() + Math.cos(a) * initial.getY();
//        return initial.setX(x).setY(y);
//    }

}
