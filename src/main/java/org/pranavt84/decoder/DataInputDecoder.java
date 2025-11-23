package org.pranavt84.decoder;

import org.pranavt84.decode.DecodeType;
import org.pranavt84.model.DataInput;
import org.pranavt84.model.DecodeResult;
import org.pranavt84.type.InputType;

public class DataInputDecoder implements Decoder<DataInput> {

    private final DecodeType decodeString;
    private final DecodeType decodeInt32;

    public DataInputDecoder(DecodeType decodeString, DecodeType decodeInt32) {
        this.decodeInt32 = decodeInt32;
        this.decodeString = decodeString;
    }

    @Override
    public DataInput decode(byte[] bytes) {
        DecodeResult result = decodeInternal(bytes, 0);
        return (DataInput) result.value;
    }

    private DecodeResult decodeInternal(byte[] bytes, int index) {

        InputType type = InputType.fromByte(bytes[index]);

        switch (type) {

            case INT32:
                return decodeInt32.decode(bytes, index);

            case STRING:
                return decodeString.decode(bytes, index);

            case DATA_INPUT:
                return decodeDataInput(bytes, index);

            default:
                throw new IllegalArgumentException("Unhandled type: " + type);
        }
    }


    /** Decode ARRAY / DataInput */
    private DecodeResult decodeDataInput(byte[] bytes, int index) {

        int firstByte = ((bytes[index + 1] & 0xFF) << 8);
        int secondByte = (bytes[index + 2] & 0xFF);
        int count = firstByte | secondByte;

        DataInput dataInput = new DataInput();

        int pos = index + 3;

        for (int i = 0; i < count; i++) {
            DecodeResult item = decodeInternal(bytes, pos);
            dataInput.add(item.value);
            pos = item.nextIndex;
        }

        return new DecodeResult(dataInput, pos);
    }
}
