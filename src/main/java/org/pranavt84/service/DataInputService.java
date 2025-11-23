package org.pranavt84.service;

import org.pranavt84.decoder.DataInputDecoder;
import org.pranavt84.encoder.DataInputEncoder;
import org.pranavt84.model.DataInput;

public class DataInputService {
    
    private final DataInputEncoder encoder;
    private final DataInputDecoder decoder;

    public DataInputService(DataInputEncoder encoder, DataInputDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public String encode(DataInput input) {

        byte[] binaryEncoded = encoder.encode(input);

        String hexEncoded = HexConverter.bytesToHex(binaryEncoded);
        
        return hexEncoded;
    }

    public DataInput decode(String encoded) {

        byte[] binaryEncoded = HexConverter.hexToBytes(encoded);

        DataInput decoded = decoder.decode(binaryEncoded);
        
        return decoded;
    }
}