package org.pranavt84.encode;

import org.pranavt84.type.InputType;

import java.nio.charset.StandardCharsets;

public class EncodeString implements EncodeType<String> {

    @Override
    public byte[] encode(String input) {

        if (input == null) return new byte[0];

        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;

        // 1 byte type + 4 bytes length + string bytes
        byte[] result = new byte[1 + 4 + length];

        result[0] = (byte) InputType.STRING.getValue();

        result[1] = (byte) ((length >> 24) & 0xFF);
        result[2] = (byte) ((length >> 16) & 0xFF);
        result[3] = (byte) ((length >> 8) & 0xFF);
        result[4] = (byte) (length & 0xFF);

        System.arraycopy(bytes, 0, result, 5, length);

        return result;
    }
}
