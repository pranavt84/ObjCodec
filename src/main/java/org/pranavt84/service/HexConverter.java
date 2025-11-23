package org.pranavt84.service;

public class HexConverter {
    
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_ARRAY[v >>> 4];
            hexChars[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Invalid length. Hex is not valid.");
        }
        
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = hexCharToValue(hex.charAt(i));
            int low = hexCharToValue(hex.charAt(i + 1));
            bytes[i / 2] = (byte) ((high << 4) | low);
        }
        return bytes;
    }
    
    private static int hexCharToValue(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        throw new IllegalArgumentException("Invalid hex character: " + c);
    }
}