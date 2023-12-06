package net.uoit.sofe4790.project.client.rpc;

import net.uoit.sofe4790.project.client.debug.HelloLocalService;
import net.uoit.sofe4790.project.client.debug.IHelloService;
import net.uoit.sofe4790.project.client.file.FileLocalService;
import net.uoit.sofe4790.project.client.file.IFileService;
import net.uoit.sofe4790.project.client.robot.IRobotService;
import net.uoit.sofe4790.project.client.robot.RobotLocalService;
import net.uoit.sofe4790.project.common.SocketWrapper;
import net.uoit.sofe4790.project.common.message.*;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcNodeInfo;
import net.uoit.sofe4790.project.common.rpc.RpcResult;
import net.uoit.sofe4790.project.server.RpcNode;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class RpcClient {
    private SocketWrapper socketWrapper;

    private final Object sendQueueLock;
    private final Queue<Message> sendQueue;

    private final Object sendLock;

    private final Map<Integer, RpcLocalService> localServices;

    private final Object remoteNodesLock;
    private final Map<Integer, RpcRemoteNode> remoteNodes;

    private final Object outstandingCallsLock;
    private final Map<Integer, CompletableFuture<RpcResult>> outstandingCalls;
    private int callCount;

    public Consumer<LoginResponseFailMessage> loginFailCallback;
    public Consumer<LoginResponseSuccessMessage> loginSuccessCallback;
    public Consumer<RpcNodeInfo> nodeConnectCallback;
    public Consumer<RpcNodeInfo> nodeDisconnectCallback;

    private boolean isConnected;

    public RpcClient() {
        // Set all initial values for the class variables.

        sendQueueLock = new Object();
        sendQueue = new LinkedList<>();

        sendLock = new Object();

        localServices = new HashMap<>();
        localServices.put(IHelloService.SERVICE_ID, new HelloLocalService());
        localServices.put(IFileService.SERVICE_ID, new FileLocalService());
        localServices.put(IRobotService.SERVICE_ID, new RobotLocalService());

        remoteNodesLock = new Object();
        remoteNodes = new HashMap<>();

        outstandingCallsLock = new Object();
        outstandingCalls = new HashMap<>();
        callCount = 0;

        loginFailCallback = null;
        loginSuccessCallback = null;
        nodeConnectCallback = null;
        nodeDisconnectCallback = null;

        isConnected = false;
    }

    public void connect(String host, int port) throws IOException {
        socketWrapper = new SocketWrapper(new Socket(host, port));

        // Start the receive loop. This will handle receiving packets.
        new Thread(this::receiveLoop).start();

        // Start the send loop. This will handle sending packets.
        new Thread(this::sendLoop).start();
    }

    public void login(String username, String password, String name) throws IOException {
        // Construct a LoginRequestMessage with the parameters and send it.
        LoginRequestMessage loginMessage = new LoginRequestMessage();
        loginMessage.username = username;
        loginMessage.password = password;
        loginMessage.nodeName = name;

        socketWrapper.sendMessage(loginMessage);
    }

    private void receiveLoop() {
        try {
            while (true) {
                // Receive a message from the server.
                Message message = socketWrapper.readMessage();

                // Handle it appropriately.
                if (message instanceof LoginResponseSuccessMessage loginResponseSuccessMessage) {
                    handleLoginSuccess(loginResponseSuccessMessage);
                } else if (message instanceof LoginResponseFailMessage loginResponseFailMessage) {
                    handleLoginFailure(loginResponseFailMessage);
                } else if (message instanceof NodeConnectMessage connectMessage) {
                    handleNodeConnect(connectMessage);
                } else if (message instanceof NodeDisconnectMessage disconnectMessage) {
                    handleNodeDisconnect(disconnectMessage);
                } else if (message instanceof RpcRequestMessage rpcRequestMessage) {
                    handleRequest(rpcRequestMessage);
                } else if (message instanceof RpcResponseMessage rpcResponseMessage) {
                    handleResponse(rpcResponseMessage);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendLoop() {
        try {
            while (true) {
                // Acquire the send queue lock.
                synchronized (sendQueueLock) {
                    // Send all messages in the queue.
                    Message message;
                    while ((message = sendQueue.poll()) != null) {
                        socketWrapper.sendMessage(message);
                    }
                }

                // Run this code at 50 Hz.
                Thread.sleep(1000 / 50);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLoginSuccess(LoginResponseSuccessMessage successMessage) {
        // Populate the nodes list.
        synchronized (remoteNodesLock) {
            for (RpcNodeInfo nodeInfo : successMessage.connectedNodes) {
                remoteNodes.put(nodeInfo.id, new RpcRemoteNode(nodeInfo.id, nodeInfo.name, this));
            }
        }

        isConnected = true;

        // Execute the callback.
        if (loginSuccessCallback != null) {
            loginSuccessCallback.accept(successMessage);
        }

        System.out.println("received login success, " + remoteNodes.size() + " nodes connected");
    }

    private void handleLoginFailure(LoginResponseFailMessage failMessage) {
        // Execute the callback.
        if (loginFailCallback != null) {
            loginFailCallback.accept(failMessage);
        }
    }

    private void handleNodeConnect(NodeConnectMessage connectMessage) {
        RpcNodeInfo nodeInfo = connectMessage.nodeInfo;

        // Add this new node to the nodes list.
        synchronized (remoteNodesLock) {
            remoteNodes.put(nodeInfo.id, new RpcRemoteNode(nodeInfo.id, nodeInfo.name, this));
        }

        // Execute the callback.
        if (nodeConnectCallback != null) {
            nodeConnectCallback.accept(null);
        }

        System.out.println("remote node " + nodeInfo.id + " (" + nodeInfo.name + ") connected");
    }

    private void handleNodeDisconnect(NodeDisconnectMessage disconnectMessage) {
        RpcNodeInfo nodeInfo = disconnectMessage.nodeInfo;

        // Remove the node from the nodes list.
        synchronized (remoteNodesLock) {
            remoteNodes.remove(nodeInfo.id);
        }

        // Execute the callback.
        if (nodeDisconnectCallback != null) {
            nodeDisconnectCallback.accept(null);
        }

        System.out.println("remote node " + nodeInfo.id + " (" + nodeInfo.name + ") disconnected");
    }

    private void handleRequest(RpcRequestMessage requestMessage) {
        // Get the requested service.
        RpcLocalService localService = localServices.get(requestMessage.serviceId);

        // Execute the requested method.
        RpcResult result = localService.handleRequest(requestMessage.methodId, requestMessage.bundle);

        // Construct a response message and send it.
        RpcResponseMessage responseMessage = new RpcResponseMessage();
        responseMessage.targetClient = requestMessage.sourceClient;
        responseMessage.callId = requestMessage.callId;
        responseMessage.result = result;

        synchronized (sendQueueLock) {
            sendQueue.add(responseMessage);
        }
    }

    private void handleResponse(RpcResponseMessage responseMessage) {
        CompletableFuture<RpcResult> future;

        // Get the CompletableFuture corresponding to the call ID.
        synchronized (outstandingCallsLock) {
            future = outstandingCalls.remove(responseMessage.callId);
        }

        // Set the result on the CompletableFuture.
        future.complete(responseMessage.result);
    }

    public RpcResult makeCall(int targetClient, int serviceId, int methodId, RpcBundle bundle) {
        // Construct a request message.
        RpcRequestMessage requestMessage = new RpcRequestMessage();
        requestMessage.sourceClient = 0xFF;
        requestMessage.targetClient = targetClient;
        requestMessage.serviceId = serviceId;
        requestMessage.methodId = methodId;
        requestMessage.bundle = bundle;

        CompletableFuture<RpcResult> future = new CompletableFuture<>();

        // Create a unique call ID and put the CompletableFuture in the outstanding calls dictionary.
        synchronized (outstandingCallsLock) {
            int callId = callCount++;

            requestMessage.callId = callId;

            outstandingCalls.put(callId, future);
        }

        try {
            // Send the request message.
            synchronized (sendLock) {
                socketWrapper.sendMessage(requestMessage);
            }

            // Wait until the future has a result, and set it.
            return future.get();
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public RpcRemoteNode getNode(int id) {
        synchronized (remoteNodesLock) {
            return remoteNodes.get(id);
        }
    }

    public HashMap<Integer, RpcRemoteNode> getNodes() {
        HashMap<Integer, RpcRemoteNode> copiedNodes;

        // Create a copy of the nodes list.
        synchronized (remoteNodesLock) {
            copiedNodes = new HashMap<Integer, RpcRemoteNode>(remoteNodes);
        }

        return copiedNodes;
    }

    public boolean getIsConnected() {
        return isConnected;
    }
}
