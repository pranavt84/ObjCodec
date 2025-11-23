package org.pranavt84.decode;

import org.pranavt84.model.DecodeResult;

public class DecodeInt32 implements DecodeType {

    @Override
    public DecodeResult decode(byte[] bytes, int index) {

        int value =
                ((bytes[index + 1] & 0xFF) << 24) |
                ((bytes[index + 2] & 0xFF) << 16) |
                ((bytes[index + 3] & 0xFF) << 8) |
                (bytes[index + 4] & 0xFF);

        return new DecodeResult(value, index + 5);
    }
}
