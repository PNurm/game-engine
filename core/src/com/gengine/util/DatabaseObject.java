package com.gengine.util;


import java.nio.ByteBuffer;

public interface DatabaseObject {
   /* public static <T extends DatabaseObject> T copy(T in) {
        try {
            @SuppressWarnings("unchecked")
            T output = (T) in.getClass().newInstance();
            return copy(in, output);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends DatabaseObject> T copy(T in, T out) {
        ByteBuffer tmp = ByteBuffer.allocate(in.length());
        in.pack(tmp);
        tmp.flip();
        out.unpack(tmp);
        return out;
    }*/

    public int length();

    public void pack(ByteBuffer dest);

    public void unpack(ByteBuffer src);
}
