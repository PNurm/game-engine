package com.gengine.core.util;

import com.badlogic.gdx.math.*;

import java.util.HashMap;
import java.util.Map;


public class Heap {


    private static int Vector3Count = 0;
    private static final Vector3[] Vector3Cache = new Vector3[128];

    private static int Vector2Count = 0;
    private static final Vector2[] Vector2Cache = new Vector2[16];

    private static int Matrix4Count = 0;
    private static final Matrix4[] Matrix4Cache = new Matrix4[16];

    private static int Matrix3Count = 0;
    private static final Matrix3[] Matrix3Cache = new Matrix3[16];

    static {
        while (Vector3Count < Vector3Cache.length) {
            Vector3Cache[Vector3Count++] = new Vector3();
        }
        while (Matrix4Count < Matrix4Cache.length) {
            Matrix4Cache[Matrix4Count++] = new Matrix4();
        }
        while (Matrix3Count < Matrix3Cache.length) {
            Matrix3Cache[Matrix3Count++] = new Matrix3();
        }
    }

    private static final boolean HEAP_WATCH = false;
    private static final Map<Integer, Data> owner = new HashMap<>();

    public static void checkin(Vector3... vs) {
        for (Vector3 v : vs) {
            if (v == null || Vector3Count >= Vector3Cache.length)
                continue;
            Vector3Cache[Vector3Count++] = v;
            if (HEAP_WATCH)
                unwatch(v);
        }
    }
    public static void checkin(Vector2... vs) {
        for (Vector2 v : vs) {
            if (v == null || Vector2Count >= Vector2Cache.length)
                continue;
            Vector2Cache[Vector2Count++] = v;
            if (HEAP_WATCH)
                unwatch(v);
        }
    }
    public static Vector2 checkout2() {
        if (Vector2Count <= 0) {
            return new Vector2();
        }
        Vector2 vector2 = Vector2Cache[--Vector2Count];
        if (HEAP_WATCH)
            watch(vector2);
        return vector2;
    }

    public static Vector3 checkout3() {
        if (Vector3Count <= 0) {
            return new Vector3();
        }
        Vector3 vector3 = Vector3Cache[--Vector3Count];
        if (HEAP_WATCH)
            watch(vector3);
        return vector3;
    }

    public static void printHeapDebug() {
        if (HEAP_WATCH) {
            for (Map.Entry<Integer, Data> e : owner.entrySet()) {
                System.err.println(e.getValue().o.getClass().getSimpleName());
                for (int k = 3; k < Math.min(10, e.getValue().d.length); k++) {
                    System.err.println(" " + e.getValue().d[k]);
                }
            }
        }
    }

    private static void unwatch(Object o) {
        owner.remove(System.identityHashCode(o));
    }

    private static void watch(Object o) {
        owner.put(System.identityHashCode(o), new Data(o));
    }

    private static class Data {
        private StackTraceElement[] d;
        private Object o;

        public Data(Object o) {
            this.o = o;
            this.d = Thread.currentThread().getStackTrace();
        }
    }
}
