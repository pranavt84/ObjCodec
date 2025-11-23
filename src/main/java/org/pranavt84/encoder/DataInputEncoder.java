package org.pranavt84.encoder;

import org.pranavt84.encode.EncodeType;
import org.pranavt84.model.DataInput;
import org.pranavt84.type.InputType;

import java.io.ByteArrayOutputStream;

public class DataInputEncoder implements Encoder<DataInput> {

    final private EncodeType encodeString;
    final private EncodeType encodeInt32;

    public DataInputEncoder(EncodeType encodeString, EncodeType encodeInt32) {
        this.encodeString = encodeString;
        this.encodeInt32 = encodeInt32;
    }

    @Override
    public byte[] encode(DataInput input) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write(InputType.DATA_INPUT.getValue());

        int size = input.getElements().size();
        out.write((size >> 8) & 0xFF);
        out.write(size & 0xFF);

        for (Object elem : input.getElements()) {
            if (elem instanceof Integer) {
                out.writeBytes(encodeInt32.encode(elem));
            } else if (elem instanceof String) {
                out.writeBytes(encodeString.encode(elem));
            } else if (elem instanceof DataInput) {
                out.writeBytes(encode((DataInput) elem));
            }
        }

        return out.toByteArray();
    }
}

