package org.pranavt84.encode;

import org.pranavt84.type.InputType;

public class EncodeInt32 implements EncodeType<Integer>{

    @Override
    public byte[] encode(Integer value) {
        byte[] bytes = new byte[1 + 4];
        bytes[0] = (byte) InputType.INT32.getValue();
        bytes[1] = (byte) ((value >> 24) & 0xFF);
        bytes[2] = (byte) ((value >> 16) & 0xFF);
        bytes[3] = (byte) ((value >> 8) & 0xFF);
        bytes[4] = (byte) (value & 0xFF);
        return bytes;
    }

}
