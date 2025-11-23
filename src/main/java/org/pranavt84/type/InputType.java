package org.pranavt84.type;

public enum InputType {
    INT32(0x01),
    STRING(0x02),
    DATA_INPUT(0x03);

    private final int value;

    InputType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static InputType fromByte(byte b) {
        int value = b & 0xFF;
        for (InputType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown type tag: " + value);
    }
}
