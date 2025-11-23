package org.pranavt84.encoder;

public interface Encoder<T> {
    byte[] encode(T t);
}
