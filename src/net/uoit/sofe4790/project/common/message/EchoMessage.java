package net.uoit.sofe4790.project.common.message;

import net.uoit.sofe4790.project.common.util.DataStreamExtensions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Message for debugging. Bi-directional.
public class EchoMessage extends Message {
    public String str;

    @Override
    protected int getMessageType() {
        return Message.TYPE_ECHO;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {
        DataStreamExtensions.writeUtf8String(stream, str);
    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {
        str = DataStreamExtensions.readUtf8String(stream);
    }
}
