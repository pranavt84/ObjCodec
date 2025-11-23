package org.pranavt84.decoder;

public interface Decoder <T> {
    public T decode(byte[] bytes);
}
