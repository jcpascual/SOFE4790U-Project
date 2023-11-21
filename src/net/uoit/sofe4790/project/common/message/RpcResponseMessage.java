package net.uoit.sofe4790.project.common.message;

import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;
import net.uoit.sofe4790.project.common.util.DataStreamExtensions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RpcResponseMessage extends Message {
    public int targetClient;
    public int callId;
    public RpcResult result;

    @Override
    protected int getMessageType() {
        return Message.TYPE_RPC_RESPONSE;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {
        stream.writeInt(targetClient);
        stream.writeInt(callId);

        stream.writeBoolean(result.success);

        byte[] serializedBundle = result.bundle.serialize();
        DataStreamExtensions.writeByteArray(stream, serializedBundle);
    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {
        targetClient = stream.readInt();
        callId = stream.readInt();

        result = new RpcResult();
        result.success = stream.readBoolean();

        byte[] serializedBundle = DataStreamExtensions.readByteArray(stream);
        result.bundle = RpcBundle.deserialize(serializedBundle);
    }
}
