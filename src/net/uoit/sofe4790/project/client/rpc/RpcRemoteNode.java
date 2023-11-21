package net.uoit.sofe4790.project.client.rpc;

import net.uoit.sofe4790.project.client.debug.HelloRemoteService;
import net.uoit.sofe4790.project.client.debug.IHelloService;
import net.uoit.sofe4790.project.client.file.FileRemoteService;
import net.uoit.sofe4790.project.client.file.IFileService;
import net.uoit.sofe4790.project.client.robot.IRobotService;
import net.uoit.sofe4790.project.client.robot.RobotRemoteService;

import java.util.HashMap;

public class RpcRemoteNode {
    public final int id;
    public final String name;

    private final HashMap<Integer, RpcRemoteService> services;

    public RpcRemoteNode(int id, String name, RpcClient client) {
        this.id = id;
        this.name = name;

        services = new HashMap<>();
        services.put(IHelloService.SERVICE_ID, new HelloRemoteService(id, client));
        services.put(IFileService.SERVICE_ID, new FileRemoteService(id, client));
        services.put(IRobotService.SERVICE_ID, new RobotRemoteService(id, client));
    }

    public RpcRemoteService getService(int id) {
        return services.get(id);
    }
}
