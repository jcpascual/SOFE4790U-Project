package net.uoit.sofe4790.project.common.message;

import net.uoit.sofe4790.project.client.rpc.RpcRemoteNode;
import net.uoit.sofe4790.project.common.rpc.RpcNodeInfo;
import net.uoit.sofe4790.project.common.util.DataStreamExtensions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginResponseSuccessMessage extends Message {
    public List<RpcNodeInfo> connectedNodes;

    @Override
    protected int getMessageType() {
        return Message.TYPE_LOGIN_RESPONSE_SUCCESS;
    }

    @Override
    protected void serializeBody(DataOutputStream stream) throws IOException {
        stream.writeInt(connectedNodes.size());

        for (RpcNodeInfo nodeInfo : connectedNodes) {
            stream.writeInt(nodeInfo.id);
            DataStreamExtensions.writeUtf8String(stream, nodeInfo.name);
        }
    }

    @Override
    protected void deserializeBody(DataInputStream stream) throws IOException {
        connectedNodes = new ArrayList<>();

        int size = stream.readInt();

        for (int i = 0; i < size; i++) {
            int id = stream.readInt();
            String name = DataStreamExtensions.readUtf8String(stream);

            connectedNodes.add(new RpcNodeInfo(id, name));
        }
    }
}
