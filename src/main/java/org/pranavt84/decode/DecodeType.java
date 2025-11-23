package org.pranavt84.decode;

import org.pranavt84.model.DecodeResult;

public interface DecodeType {

    public DecodeResult decode(byte[] bytes, int index);
}
