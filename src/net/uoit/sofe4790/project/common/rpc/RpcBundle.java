package net.uoit.sofe4790.project.common.rpc;

import net.uoit.sofe4790.project.common.util.DataStreamExtensions;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class RpcBundle {
    private static final int SERIALIZED_TYPE_INT = 0x00;
    private static final int SERIALIZED_TYPE_STRING = 0x01;
    private static final int SERIALIZED_TYPE_BOOL = 0x02;
    private static final int SERIALIZED_TYPE_BYTE = 0x03;

    private static final int SERIALIZED_TYPE_STRING_ARRAY = 0x11;
    private static final int SERIALIZED_TYPE_BYTE_ARRAY = 0x03;

    private final Map<String, Object> values;

    // Getters and setters.

    public RpcBundle() {
        values = new HashMap<>();
    }

    public int getInt(String key) {
        return (Integer)values.get(key);
    }

    public void putInt(String key, int value) {
        values.put(key, value);
    }

    public String getString(String key) {
        return (String)values.get(key);
    }

    public void putString(String key, String value) {
        values.put(key, value);
    }

    public boolean getBoolean(String key) {
        return (Boolean) values.get(key);
    }

    public void putBoolean(String key, boolean value) {
        values.put(key, value);
    }

    public String[] getStringArray(String key) {
        return (String[])values.get(key);
    }

    public void putStringArray(String key, String[] value) {
        values.put(key, value);
    }

    public byte[] getByteArray(String key) {
        return (byte[])values.get(key);
    }

    public void putByteArray(String key, byte[] value) {
        values.put(key, value);
    }

    // Serialization.

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream innerStream = new ByteArrayOutputStream();
        DataOutputStream outerStream = new DataOutputStream(innerStream);

        // Write the number of key-value pairs.
        outerStream.writeInt(values.size());

        // Write each key to the output.
        for (String key : values.keySet()) {
            Object value = values.get(key);

            // Write the key's name.
            DataStreamExtensions.writeUtf8String(outerStream, key);

            // Write the value.
            if (value instanceof Integer) {
                outerStream.write(SERIALIZED_TYPE_INT);
                outerStream.writeInt((Integer)value);
            } else if (value instanceof String) {
                outerStream.write(SERIALIZED_TYPE_STRING);
                DataStreamExtensions.writeUtf8String(outerStream, (String) value);
            } else if (value instanceof Boolean) {
                outerStream.write(SERIALIZED_TYPE_BOOL);
                outerStream.writeBoolean((Boolean)value);
            } else if (value instanceof String[]) {
                outerStream.write(SERIALIZED_TYPE_STRING_ARRAY);

                String[] valCast = (String[]) value;

                // Write the array length.
                outerStream.writeInt(valCast.length);

                // Write each element.
                for (String str : valCast) {
                    DataStreamExtensions.writeUtf8String(outerStream, str);
                }
            } else if (value instanceof byte[]) {
                outerStream.write(SERIALIZED_TYPE_BYTE_ARRAY);

                byte[] valCast = (byte[])value;

                // Write the array length.
                outerStream.writeInt(valCast.length);

                // Write all the bytes.
                outerStream.write(valCast);
            } else {
                throw new RuntimeException("Invalid type");
            }
        }

        return innerStream.toByteArray();
    }

    public static RpcBundle deserialize(byte[] data) throws IOException {
        // Create a new ByteArrayInputStream using the byte array as the input.
        ByteArrayInputStream innerStream = new ByteArrayInputStream(data);
        return deserialize(innerStream);
    }

    public static RpcBundle deserialize(InputStream innerStream) throws IOException {
        DataInputStream outerStream = new DataInputStream(innerStream);

        RpcBundle bundle = new RpcBundle();

        // Get the number of key-value pairs.
        int size = outerStream.readInt();

        // Read each pair.
        for (int i = 0; i < size; i++) {
            // Read the key.
            String key = DataStreamExtensions.readUtf8String(outerStream);
            Object value;

            // Read the value type.
            int type = outerStream.read();

            // Read the value.
            if (type == SERIALIZED_TYPE_INT) {
                value = outerStream.readInt();
            } else if (type == SERIALIZED_TYPE_STRING) {
                value = DataStreamExtensions.readUtf8String(outerStream);
            } else if (type == SERIALIZED_TYPE_BOOL) {
                value = outerStream.readBoolean();
            } else if (type == SERIALIZED_TYPE_STRING_ARRAY) {
                int arraySize = outerStream.readInt();

                String[] valCast = new String[arraySize];

                for (int j = 0; j < arraySize; j++) {
                    valCast[j] = DataStreamExtensions.readUtf8String(outerStream);
                }

                value = valCast;
            } else if (type == SERIALIZED_TYPE_BYTE_ARRAY) {
                int arraySize = outerStream.readInt();

                value = outerStream.readNBytes(arraySize);
            } else {
                throw new RuntimeException("Invalid type");
            }

            // Store the pair in the reconstructed bundle.
            bundle.values.put(key, value);
        }

        return bundle;
    }
}
