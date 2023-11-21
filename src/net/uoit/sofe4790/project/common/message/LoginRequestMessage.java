package net.uoit.sofe4790.project.common.message;

import net.uoit.sofe4790.project.common.util.DataStreamExtensions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Sent when the client first logs in.
public class LoginRequestMessage extends Message {
    public String username;
    public String password;
    public String nodeName;

    @Override
    protected int getMessageType() {
        return Message.TYPE_LOGIN_REQUEST;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {
        DataStreamExtensions.writeUtf8String(stream, username);
        DataStreamExtensions.writeUtf8String(stream, password);
        DataStreamExtensions.writeUtf8String(stream, nodeName);
    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {
        username = DataStreamExtensions.readUtf8String(stream);
        password = DataStreamExtensions.readUtf8String(stream);
        nodeName = DataStreamExtensions.readUtf8String(stream);
    }
}
