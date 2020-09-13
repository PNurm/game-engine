package com.gengine.core.util;


import java.nio.ByteBuffer;

public interface DatabaseObject {
    public int length();

    public void pack(ByteBuffer dest);

    public void unpack(ByteBuffer src);
}
