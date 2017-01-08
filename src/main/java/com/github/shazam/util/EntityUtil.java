package com.github.shazam.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

import static com.github.shazam.util.ReflectionUtil.getServerVersion;

public class EntityUtil {

    public static class BoundingBox {

        private final Vector point1;
        private final Vector point2;

        BoundingBox(Vector point1, Vector point2) {
            this.point1 = point1;
            this.point2 = point2;
        }

        public boolean contains(Vector vector) {
            boolean inX = containsAxis(point1.getX(), point2.getX(), vector.getX());
            boolean inY = containsAxis(point1.getY(), point2.getY(), vector.getY());
            boolean inZ = containsAxis(point1.getZ(), point2.getZ(), vector.getZ());
            return inX && inY && inZ;
        }

        public boolean inRadius(Vector vector, double radius) {
            boolean inX = inRadiusAxis(point1.getX(), point2.getX(), vector.getX(), radius);
            boolean inY = inRadiusAxis(point1.getY(), point2.getY(), vector.getY(), radius);
            boolean inZ = inRadiusAxis(point1.getZ(), point2.getZ(), vector.getZ(), radius);
            return inX && inY && inZ;
        }

        public Vector getPoint1() {
            return point1;
        }

        public Vector getPoint2() {
            return point2;
        }

        private boolean containsAxis(double point1, double point2, double testPoint) {
            return point1 > point2 ?
                    point1 > testPoint && testPoint > point2 :
                    point1 < testPoint && testPoint < point2;
        }

        private boolean inRadiusAxis(double point1, double point2, double testPoint, double radius) {
            return point1 > point2 ?
                    (point1 > (testPoint - radius) && (testPoint + radius) > point2) :
                    (point1 < (testPoint + radius) && (testPoint - radius) < point2);
        }

    }

    public static BoundingBox getBoundingBox(Entity entity) {
        try {
//            final Class<Entity> clazzEntity = Entity.class;
            final String version = getServerVersion();
            final Class<?> clazzCraftEntity = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");

            final Method getHandle = clazzCraftEntity.getMethod("getHandle");
            final Object craftEntity = getHandle.invoke(clazzCraftEntity.cast(entity));

            final Class<?> clazzHandle = craftEntity.getClass();
            final Method getBoundingBox = clazzHandle.getMethod("getBoundingBox");
            final Object boundingBox = getBoundingBox.invoke(craftEntity);

            final Class<?> clazzBoundingBox = boundingBox.getClass();
            final double fieldA = (double) clazzBoundingBox.getDeclaredField("a").get(boundingBox);
            final double fieldB = (double) clazzBoundingBox.getDeclaredField("b").get(boundingBox);
            final double fieldC = (double) clazzBoundingBox.getDeclaredField("c").get(boundingBox);
            final double fieldD = (double) clazzBoundingBox.getDeclaredField("d").get(boundingBox);
            final double fieldE = (double) clazzBoundingBox.getDeclaredField("e").get(boundingBox);
            final double fieldF = (double) clazzBoundingBox.getDeclaredField("f").get(boundingBox);

            return new BoundingBox(
                    new Vector(fieldA, fieldB, fieldC),
                    new Vector(fieldD, fieldE, fieldF)
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get BoundingBox: ", ex);
        }
    }

}

