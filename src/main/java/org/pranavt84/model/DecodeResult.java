package org.pranavt84.model;

public class DecodeResult {
    public Object value;
    public int nextIndex;

    public DecodeResult(Object value, int nextIndex) {
        this.value = value;
        this.nextIndex = nextIndex;
    }
}
