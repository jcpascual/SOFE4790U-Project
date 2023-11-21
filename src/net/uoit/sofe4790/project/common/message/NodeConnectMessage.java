package net.uoit.sofe4790.project.common.message;

import net.uoit.sofe4790.project.common.rpc.RpcNodeInfo;
import net.uoit.sofe4790.project.common.util.DataStreamExtensions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NodeConnectMessage extends Message {
    public RpcNodeInfo nodeInfo;

    @Override
    protected int getMessageType() {
        return Message.TYPE_NODE_CONNECT;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {
        stream.writeInt(nodeInfo.id);
        DataStreamExtensions.writeUtf8String(stream, nodeInfo.name);
    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {
        int id = stream.readInt();
        String name = DataStreamExtensions.readUtf8String(stream);

        nodeInfo = new RpcNodeInfo(id, name);
    }
}
