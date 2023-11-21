package net.uoit.sofe4790.project.client.debug;

import net.uoit.sofe4790.project.client.rpc.RpcLocalService;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

public class HelloLocalService extends RpcLocalService implements IHelloService {
    @Override
    public String getHello() {
        return "Hello, World!";
    }

    @Override
    public String getHelloWithParameter(String param) {
        return "Hello, " + param + "!";
    }

    @Override
    public RpcResult handleRequest(int methodId, RpcBundle bundle) {
        RpcResult result = new RpcResult();
        result.success = true;

        if (methodId == METHOD_ID_GET_HELLO) {
            result.bundle.putString(RETURN_VALUE, getHello());
        } else if (methodId == METHOD_ID_GET_HELLO_WITH_PARAMETER) {
            String param = bundle.getString("param");
            String returnValue = getHelloWithParameter(param);

            result.bundle.putString(RETURN_VALUE, returnValue);
        } else {
            throw new RuntimeException("Invalid method ID");
        }

        return result;
    }
}
