package net.uoit.sofe4790.project.common.message;


import java.io.*;

public abstract class Message {
    // Constants for message IDs.

    public static final int TYPE_LOGIN_REQUEST = 0x10;
    public static final int TYPE_LOGIN_RESPONSE_SUCCESS = 0x11;
    public static final int TYPE_LOGIN_RESPONSE_FAIL = 0x12;
    public static final int TYPE_GOODBYE = 0x12;

    public static final int TYPE_NODE_CONNECT = 0x20;
    public static final int TYPE_NODE_DISCONNECT = 0x21;

    public static final int TYPE_RPC_REQUEST = 0xE0;
    public static final int TYPE_RPC_RESPONSE = 0xE1;

    public static final int TYPE_ECHO = 0xFF; // for debug

    protected Message() {
        //
    }

    protected abstract int getMessageType();

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream innerStream = new ByteArrayOutputStream();
        DataOutputStream outerStream = new DataOutputStream(innerStream);

        outerStream.write(getMessageType());

        serializeBody(outerStream);

        return innerStream.toByteArray();
    }

    public static Message deserialize(InputStream outerStream) throws IOException {
        DataInputStream innerStream = new DataInputStream(outerStream);

        int type = innerStream.read();

        Message message = null;

        if (type == TYPE_LOGIN_REQUEST) {
            message = new LoginRequestMessage();
        } else if (type == TYPE_LOGIN_RESPONSE_SUCCESS) {
            message = new LoginResponseSuccessMessage();
        } else if (type == TYPE_LOGIN_RESPONSE_FAIL) {
            message = new LoginResponseFailMessage();
        } else if (type == TYPE_NODE_CONNECT) {
            message = new NodeConnectMessage();
        } else if (type == TYPE_NODE_DISCONNECT) {
            message = new NodeDisconnectMessage();
        } else if (type == TYPE_RPC_REQUEST) {
            message = new RpcRequestMessage();
        } else if (type == TYPE_RPC_RESPONSE) {
            message = new RpcResponseMessage();
        } else if (type == TYPE_ECHO) {
            message = new EchoMessage();
        } else {
            throw new RuntimeException("Invalid message type " + type);
        }

        message.deserializeBody(innerStream);

        return message;
    }

    protected abstract void serializeBody(DataOutputStream stream) throws IOException;

    protected abstract void deserializeBody(DataInputStream stream) throws IOException;
}
