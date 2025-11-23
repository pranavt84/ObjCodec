package org.pranavt84.service;

import org.pranavt84.decode.DecodeInt32;
import org.pranavt84.decode.DecodeString;
import org.pranavt84.decoder.DataInputDecoder;
import org.pranavt84.encode.EncodeInt32;
import org.pranavt84.encode.EncodeString;
import org.pranavt84.encoder.DataInputEncoder;

public class ServiceFactory {

    public static DataInputService createService() {

        EncodeString encodeString = new EncodeString();
        EncodeInt32 encodeInt32 = new EncodeInt32();

        DecodeString decodeString = new DecodeString();
        DecodeInt32 decodeInt32 = new DecodeInt32();

        DataInputEncoder encoder = new DataInputEncoder(encodeString, encodeInt32);

        DataInputDecoder decoder = new DataInputDecoder(decodeString, decodeInt32);

        return new DataInputService(encoder, decoder);
    }

    private ServiceFactory() {
        throw new AssertionError("ServiceFactory should not be instantiated, with default constructor.");
    }
}