package net.uoit.sofe4790.project.common.message;

import net.uoit.sofe4790.project.common.rpc.RpcBundle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RpcRequestMessage extends Message {
    public int sourceClient;
    public int targetClient;
    public int callId;
    public int serviceId;
    public int methodId;
    public RpcBundle bundle;

    @Override
    protected int getMessageType() {
        return Message.TYPE_RPC_REQUEST;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {
        stream.write(sourceClient);
        stream.write(targetClient);
        stream.writeInt(callId);
        stream.write(serviceId);
        stream.write(methodId);
        stream.write(bundle.serialize());
    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {
        sourceClient = stream.read();
        targetClient = stream.read();
        callId = stream.readInt();
        serviceId = stream.read();
        methodId = stream.read();
        bundle = RpcBundle.deserialize(stream);
    }
}
