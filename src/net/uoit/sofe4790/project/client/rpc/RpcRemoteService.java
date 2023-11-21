package net.uoit.sofe4790.project.client.rpc;

public class RpcRemoteService {
    protected int target;
    protected RpcClient client;

    protected RpcRemoteService(int target, RpcClient client) {
        this.target = target;
        this.client = client;
    }
}
