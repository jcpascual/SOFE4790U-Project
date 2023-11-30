package net.uoit.sofe4790.project.client;

import net.uoit.sofe4790.project.client.rpc.RpcClient;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteNode;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteService;
import net.uoit.sofe4790.project.common.rpc.RpcNodeInfo;

import java.io.IOException;
import java.util.HashMap;

public class ClientHelper {
    public static final ClientHelper instance = new ClientHelper();

    private RpcClient client;
    private int targetNode;

    private ClientHelper() {
        client = null;
    }

    public void connect(String host, int port) throws IOException {
        client = new RpcClient();
        client.connect(host, port);

        targetNode = -1;
    }

    public void login(String username, String password, String name) throws IOException {
        client.login(username, password, name);
    }

    public RpcClient getClient() {
        return client;
    }

    public HashMap<Integer, RpcRemoteNode> getNodes() {
        return client.getNodes();
    }

    public int getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(int target) {
        targetNode = target;
    }

    public RpcRemoteService getTargetService(int id) {
        return client.getNode(targetNode).getService(id);
    }
}
