package net.uoit.sofe4790.project.server;

import net.uoit.sofe4790.project.common.message.*;
import net.uoit.sofe4790.project.common.rpc.RpcNodeInfo;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpcRelayServer {
    private static final String AUTHORIZED_USERS_PATH = "users.txt";

    // Contains the users that are authorized to connect.
    private AuthorizedUsers users;

    // Node information.
    private final Object nodesLock;
    private final Map<Integer, RpcNode> nodes;
    private int nodeCount;

    public RpcRelayServer() {
        // Load the list of authorized users and their passwords.
        users = new AuthorizedUsers(AUTHORIZED_USERS_PATH);

        // Populate the initial node information values.
        nodesLock = new Object();
        nodes = new HashMap<>();
        nodeCount = 0;
    }

    public void listen() throws IOException {
        // Open a ServerSocket.
        ServerSocket serverSocket = new ServerSocket(3500);

        // Enter an infinite loop.
        while (true) {
            // Accept any incoming connections.
            Socket clientSocket = serverSocket.accept();

            RpcNode node;

            // Create a new node instance with its own unique ID.
            synchronized (nodesLock) {
                node = new RpcNode(nodeCount++, clientSocket);
            }

            System.out.println("Node " + node.id + " connected");

            // Spawn a new Thread dedicated to receiving data from this client.
            new Thread(() -> receiveLoop(node)).start();
        }
    }

    private void receiveLoop(RpcNode node) {
        try {
            while (true) {
                // Attempt to read a message.
                Message message = node.readMessage();

                // Handle the message.
                if (message instanceof LoginRequestMessage loginRequestMessage) {
                    handleLoginRequest(node, loginRequestMessage);
                } else if (message instanceof RpcRequestMessage rpcRequestMessage) {
                    handleRequest(node, rpcRequestMessage);
                } else if (message instanceof RpcResponseMessage rpcResponseMessage) {
                    handleResponse(rpcResponseMessage);
                }
            }
        } catch (EOFException e) {
            // If the stream is closed with an EOFException, the client disconnected.
            try {
                handleDisconnect(node);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleDisconnect(RpcNode targetNode) throws IOException {
        // Create a node disconnect message to send to all connected nodes.
        NodeDisconnectMessage disconnectMessage = new NodeDisconnectMessage();
        disconnectMessage.nodeInfo = new RpcNodeInfo(targetNode.id, targetNode.name);

        synchronized (nodesLock) {
            // Remove the disconnecting node now, so we don't send a message to them.
            nodes.remove(targetNode.id);

            // Send all other nodes the message.
            for (RpcNode node : nodes.values()) {
                node.sendMessage(disconnectMessage);
            }
        }

        System.out.println("Node " + targetNode.id + " (" + targetNode.name + ") disconnected");
    }

    private void handleLoginRequest(RpcNode targetNode, LoginRequestMessage requestMessage) throws IOException {
        // Check the provided credentials.
        if (!users.checkCredentials(requestMessage.username, requestMessage.password)) {
            // Not valid. Send the login failed message.
            targetNode.sendMessage(new LoginResponseFailMessage());
            return;
        }

        // Set initial properties for this node.
        targetNode.authenticated = true;
        targetNode.name = requestMessage.nodeName;

        // Create a node connected message for later sending.
        NodeConnectMessage connectMessage = new NodeConnectMessage();
        connectMessage.nodeInfo = new RpcNodeInfo(targetNode.id, targetNode.name);

        List<RpcNodeInfo> nodeInfos = new ArrayList<>();

        synchronized (nodesLock) {
            for (RpcNode node : nodes.values()) {
                // Skip this node if they're not authenticated.
                if (!node.authenticated) {
                    continue;
                }

                // Create an RpcNodeInfo instance for this node.
                nodeInfos.add(new RpcNodeInfo(node.id, node.name));

                // Send them the node connect message.
                node.sendMessage(connectMessage);
            }

            // Add the connecting node to the nodes list, since we've already acquired the lock.
            nodes.put(targetNode.id, targetNode);
        }

        // Send the connecting node the login success message.
        LoginResponseSuccessMessage successMessage = new LoginResponseSuccessMessage();
        successMessage.connectedNodes = nodeInfos;

        targetNode.sendMessage(successMessage);

        System.out.println("Node " + targetNode.id + " (" + targetNode.name + ") authenticated");
    }

    private void handleRequest(RpcNode node, RpcRequestMessage requestMessage) throws IOException {
        // Set the source node to the sending node's ID.
        requestMessage.sourceClient = node.id;

        RpcNode targetNode;

        // Get the target node instance.
        synchronized (nodesLock) {
            targetNode = nodes.get(requestMessage.targetClient);
        }

        // Relay them the request.
        targetNode.sendMessage(requestMessage);
    }

    private void handleResponse(RpcResponseMessage responseMessage) throws IOException {
        RpcNode targetNode;

        // Get the receiving node by their ID.
        synchronized (nodesLock) {
            targetNode = nodes.get(responseMessage.targetClient);
        }

        // Relay them the response.
        targetNode.sendMessage(responseMessage);
    }
}
