package net.uoit.sofe4790.project.client.robot;

import net.uoit.sofe4790.project.client.rpc.RpcClient;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteService;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

public class RobotRemoteService extends RpcRemoteService implements IRobotService {
    public RobotRemoteService(int target, RpcClient client) {
        super(target, client);
    }

    @Override
    public byte[] getScreenshot() {
        RpcResult result = client.makeCall(target, SERVICE_ID, METHOD_ID_GET_SCREENSHOT, new RpcBundle());

        return result.bundle.getByteArray(RETURN_VALUE);
    }

    @Override
    public void click(int x, int y, boolean right) {
        // Put the parameters into an input bundle.
        RpcBundle bundle = new RpcBundle();
        bundle.putInt("x", x);
        bundle.putInt("y", y);
        bundle.putBoolean("right", right);

        client.makeCall(target, SERVICE_ID, METHOD_ID_CLICK, bundle);
    }
}
