package net.uoit.sofe4790.project.common.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DataStreamExtensions {
    // Helper method for writing Strings to a DataOutputStream.
    public static void writeUtf8String(DataOutputStream stream, String str) throws IOException {
        // Get the UTF-8 bytes of the value.
        byte[] data = str.getBytes(StandardCharsets.UTF_8);

        // Write the byte array.
        writeByteArray(stream, data);
    }

    public static String readUtf8String(DataInputStream stream) throws IOException {
        // Get the UTF-8 bytes.
        byte[] data = readByteArray(stream);

        // Construct a String instance using them.
        return new String(data, StandardCharsets.UTF_8);
    }

    public static void writeByteArray(DataOutputStream stream, byte[] b) throws IOException {
        // Write the length of the byte array.
        stream.writeInt(b.length);

        // Write the bytes.
        stream.write(b);
    }

    public static byte[] readByteArray(DataInputStream stream) throws IOException {
        // Read the number of bytes.
        int len = stream.readInt();

        // Read each byte into the byte array.
        return stream.readNBytes(len);
    }
}
