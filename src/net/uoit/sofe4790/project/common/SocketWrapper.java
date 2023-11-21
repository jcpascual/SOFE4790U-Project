package net.uoit.sofe4790.project.common;

import net.uoit.sofe4790.project.common.message.Message;

import java.io.*;
import java.net.Socket;

public class SocketWrapper {
    private final Socket socket;

    private DataInputStream inputStream = null;
    private DataOutputStream outputStream = null;

    public SocketWrapper(Socket socket) {
        this.socket = socket;
    }

    public Message readMessage() throws IOException {
        // Create the DataInputStream if necessary.
        if (inputStream == null) {
            inputStream = new DataInputStream(socket.getInputStream());
        }

        // Read the size of the message.
        int size = inputStream.readInt();

        // Read the actual data.
        byte[] data = inputStream.readNBytes(size);

        ByteArrayInputStream innerStream = new ByteArrayInputStream(data);

        // Deserialize the message.
        return Message.deserialize(innerStream);
    }

    public void sendMessage(Message message) throws IOException {
        // Create the DataOutputStream if necessary.
        if (outputStream == null) {
            outputStream = new DataOutputStream(socket.getOutputStream());
        }

        // Serialize the message.
        byte[] data = message.serialize();

        // Write the size of the serialized message, then the actual data itself.
        outputStream.writeInt(data.length);
        outputStream.write(data);
    }

    public void close() throws IOException {
        // Close all resources.

        if (outputStream != null) {
            outputStream.close();
        }

        if (inputStream != null) {
            inputStream.close();
        }

        socket.close();
    }
}