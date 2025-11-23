import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pranavt84.decode.DecodeInt32;
import org.pranavt84.decode.DecodeString;
import org.pranavt84.decoder.DataInputDecoder;
import org.pranavt84.encode.EncodeInt32;
import org.pranavt84.encode.EncodeString;
import org.pranavt84.encoder.DataInputEncoder;
import org.pranavt84.model.DataInput;
import org.pranavt84.service.DataInputService;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataInputServiceTest {

    private static DataInputEncoder encoder;
    private static DataInputDecoder decoder;
    private static DataInputService dataInputService;

    @BeforeAll
    static void setup() {
        encoder = new DataInputEncoder(new EncodeString(), new EncodeInt32());
        decoder = new DataInputDecoder(new DecodeString(), new DecodeInt32());
        dataInputService = new DataInputService(encoder, decoder);
    }

    @Test
    void testEmptyEncodingDecoding() {
        DataInput dataInput = new DataInput();
        String encoded = dataInputService.encode(dataInput);
        DataInput decoded = dataInputService.decode(encoded);

        assertEquals(0, decoded.getElements().size());
    }

    @Test
    void testIntegerEncodingDecoding() {
        int[] testValues = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE};

        for (int val : testValues) {
            DataInput dataInput = new DataInput();
            dataInput.add(val);

            String encoded = dataInputService.encode(dataInput);
            DataInput decoded = dataInputService.decode(encoded);

            assertEquals(1, decoded.getElements().size());
            assertEquals(val, decoded.getElements().get(0));
        }
    }

    @Test
    void testStringEncodingDecoding() {
        String[] strings = {"", "a", "Hello, World!", "é漢字🌟", "\u0000\uFFFF"};

        for (String s : strings) {
            DataInput dataInput = new DataInput();
            dataInput.add(s);

            String encoded = dataInputService.encode(dataInput);
            DataInput decoded = dataInputService.decode(encoded);

            assertEquals(1, decoded.getElements().size());
            assertEquals(s, decoded.getElements().get(0));
        }
    }


    @Test
    void testNestedDataInput() {
        DataInput nested = new DataInput();
        nested.add("nested");
        nested.add(123);

        DataInput root = new DataInput();
        root.add("root");
        root.add(nested);
        root.add(-42);

        String encoded = dataInputService.encode(root);
        DataInput decoded = dataInputService.decode(encoded);

        assertEquals(3, decoded.getElements().size());
        assertEquals("root", decoded.getElements().get(0));

        DataInput decodedNested = (DataInput) decoded.getElements().get(1);
        assertEquals("nested", decodedNested.getElements().get(0));
        assertEquals(123, decodedNested.getElements().get(1));

        assertEquals(-42, decoded.getElements().get(2));
    }

    @Test
    void testLargeArray() {
        DataInput root = new DataInput();
        for (int i = 0; i < 1000; i++) {
            root.add(i);
        }

        String encoded = dataInputService.encode(root);
        DataInput decoded = dataInputService.decode(encoded);

        assertEquals(1000, decoded.getElements().size());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, decoded.getElements().get(i));
        }
    }

    @Test
    void testLargeArrayString() {
        DataInput root = new DataInput();
        for (int i = 0; i < 1000; i++) {
            root.add("clickhose");
        }

        String encoded = dataInputService.encode(root);
        DataInput decoded = dataInputService.decode(encoded);

        assertEquals(1000, decoded.getElements().size());
        for (int i = 0; i < 1000; i++) {
            assertEquals("clickhose", decoded.getElements().get(i));
        }
    }

    @Test
    void testLargeString() {
        char[] chars = new char[1_000_000];
        java.util.Arrays.fill(chars, 'b');
        String longString = new String(chars);

        DataInput root = new DataInput();
        root.add(longString);

        String encoded = dataInputService.encode(root);
        DataInput decoded = dataInputService.decode(encoded);

        assertEquals(1, decoded.getElements().size());
        assertEquals(longString, decoded.getElements().get(0));
    }

    @Test
    void testMixedTypes() {
        DataInput nested = new DataInput();
        nested.add("bar");
        nested.add(99);

        DataInput root = new DataInput();
        root.add(42);
        root.add("foo");
        root.add(nested);

        String encoded = dataInputService.encode(root);
        DataInput decoded = dataInputService.decode(encoded);

        assertEquals(3, decoded.getElements().size());
        assertEquals(42, decoded.getElements().get(0));
        assertEquals("foo", decoded.getElements().get(1));

        DataInput decodedNested = (DataInput) decoded.getElements().get(2);
        assertEquals("bar", decodedNested.getElements().get(0));
        assertEquals(99, decodedNested.getElements().get(1));
    }
}
