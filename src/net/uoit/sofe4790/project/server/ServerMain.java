package net.uoit.sofe4790.project.server;

import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException {
        RpcRelayServer relayServer = new RpcRelayServer();
        relayServer.listen();
    }
}
