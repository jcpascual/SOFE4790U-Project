package net.uoit.sofe4790.project.client.rpc;

import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

public abstract class RpcLocalService {
    public abstract RpcResult handleRequest(int methodId, RpcBundle bundle);
}
