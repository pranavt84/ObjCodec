package org.pranavt84.decode;

import org.pranavt84.model.DecodeResult;

import java.nio.charset.StandardCharsets;

public class DecodeString implements DecodeType {

    @Override
    public DecodeResult decode(byte[] bytes, int index) {

        // 4 bytes length
        int length =
                ((bytes[index + 1] & 0xFF) << 24) |
                        ((bytes[index + 2] & 0xFF) << 16) |
                        ((bytes[index + 3] & 0xFF) << 8) |
                        (bytes[index + 4] & 0xFF);

        String value = new String(bytes, index + 5, length, StandardCharsets.UTF_8);

        return new DecodeResult(value, index + 5 + length);
    }
}
