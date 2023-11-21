package net.uoit.sofe4790.project.server;

import net.uoit.sofe4790.project.common.SocketWrapper;
import net.uoit.sofe4790.project.common.message.Message;

import java.io.IOException;
import java.net.Socket;

public class RpcNode {
    public final int id;

    public boolean authenticated;
    public String name;

    private final SocketWrapper socketWrapper;

    public RpcNode(int nodeId, Socket socket) {
        id = nodeId;
        authenticated = false;
        name = "Node " + id + " (unauthenticated)";
        socketWrapper = new SocketWrapper(socket);
    }

    public Message readMessage() throws IOException {
        return socketWrapper.readMessage();
    }

    public void sendMessage(Message message) throws IOException {
        socketWrapper.sendMessage(message);
    }

    public void close() {
        try {
            socketWrapper.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
