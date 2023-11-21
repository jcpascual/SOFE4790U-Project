package net.uoit.sofe4790.project.common.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DataStreamExtensions {
    public static void writeUtf8String(DataOutputStream stream, String str) throws IOException {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        writeByteArray(stream, data);
    }

    public static String readUtf8String(DataInputStream stream) throws IOException {
        byte[] data = readByteArray(stream);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static void writeByteArray(DataOutputStream stream, byte[] b) throws IOException {
        stream.writeInt(b.length);
        stream.write(b);
    }

    public static byte[] readByteArray(DataInputStream stream) throws IOException {
        int len = stream.readInt();
        byte[] data = stream.readNBytes(len);

        return data;
    }
}
