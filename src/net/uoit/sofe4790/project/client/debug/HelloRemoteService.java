package net.uoit.sofe4790.project.client.debug;

import net.uoit.sofe4790.project.client.rpc.RpcClient;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteService;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

public class HelloRemoteService extends RpcRemoteService implements IHelloService {
    public HelloRemoteService(int target, RpcClient client) {
        super(target, client);
    }

    @Override
    public String getHello() {
        RpcResult result = client.makeCall(target, SERVICE_ID, METHOD_ID_GET_HELLO, new RpcBundle());

        return result.bundle.getString(RETURN_VALUE);
    }

    @Override
    public String getHelloWithParameter(String param) {
        RpcBundle bundle = new RpcBundle();
        bundle.putString("param", param);

        RpcResult result = client.makeCall(target, SERVICE_ID, METHOD_ID_GET_HELLO_WITH_PARAMETER, bundle);

        return result.bundle.getString(RETURN_VALUE);
    }
}
