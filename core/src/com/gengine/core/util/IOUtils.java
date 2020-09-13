package com.gengine.core.util;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.io.*;
import java.nio.ByteBuffer;

public class IOUtils {

    public static void pack(Vector3 vector3, ByteBuffer data) {
        data.putFloat(vector3.x);
        data.putFloat(vector3.y);
        data.putFloat(vector3.z);
    }

    public static void unpack(Vector3 vector3, ByteBuffer data) {
        vector3.x = data.getFloat();
        vector3.y = data.getFloat();
        vector3.z = data.getFloat();
    }

    public static void pack(Matrix4 m, ByteBuffer dest) {
        for (int i = 0; i < 16; i++)
            dest.putFloat(m.getValues()[i]);
    }

    public static byte[] stringToByteArray(String message) {
        try {
            return message.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return message.getBytes();
        }
    }

    public static void pack(String s, ByteBuffer b) {
        if (s == null) {
            b.putInt(-1);
            return;
        }
        byte[] data = stringToByteArray(s);
        b.putInt(data.length);
        b.put(data);
    }

    public static int sizeof(Matrix4 m) {
        return 16 * 4;
    }

    public static int sizeof(String s) {
        if (s == null)
            return 4;
        return 4 + stringToByteArray(s).length;
    }

    public static int sizeof(Vector3 v) {
        return 3 * 4;
    }

    public static String unpackString(ByteBuffer b) {
        int l = b.getInt();
        if (l < 0)
            return null;
        byte[] data = new byte[l];
        b.get(data);
        return byteToString(data, 0, data.length);
    }

    /**
     * Decodes a byte array back into a string
     */
    public static String byteToString(byte[] data, int offset, int length) {
        try {
            return new String(data, offset, length, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(data, offset, length);
        }
    }

    private static final byte[] IO_BUFF = new byte[1024 * 1024 * 4]; // 4MB

    public static final byte[] readStream(InputStream in) throws IOException {
        byte[] data;
        if (in instanceof FileInputStream) {
            in = new BufferedInputStream(in);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } else {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            while ((nRead = in.read(IO_BUFF)) != -1) {
                buffer.write(IO_BUFF, 0, nRead);
            }
            buffer.close();
            in.close();
            data = buffer.toByteArray();
        }
        return data;
    }

}
