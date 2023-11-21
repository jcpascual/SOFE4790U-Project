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

    public void connect(String host, int port, String localName) throws IOException {
        socketWrapper = new SocketWrapper(new Socket(host, port));

        new Thread(this::receiveLoop).start();

        new Thread(this::sendLoop).start();
    }

    public void login(String username, String password, String name) throws IOException {
        LoginRequestMessage loginMessage = new LoginRequestMessage();
        loginMessage.username = username;
        loginMessage.password = password;
        loginMessage.nodeName = name;

        socketWrapper.sendMessage(loginMessage);
    }

    private void receiveLoop() {
        try {
            while (true) {
                Message message = socketWrapper.readMessage();

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
                synchronized (sendQueueLock) {
                    Message message;
                    while ((message = sendQueue.poll()) != null) {
                        socketWrapper.sendMessage(message);
                    }
                }

                Thread.sleep(1000 / 50);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleLoginSuccess(LoginResponseSuccessMessage successMessage) {
        synchronized (remoteNodesLock) {
            for (RpcNodeInfo nodeInfo : successMessage.connectedNodes) {
                remoteNodes.put(nodeInfo.id, new RpcRemoteNode(nodeInfo.id, nodeInfo.name, this));
            }
        }

        isConnected = true;

        if (loginSuccessCallback != null) {
            loginSuccessCallback.accept(successMessage);
        }

        System.out.println("received login success, " + remoteNodes.size() + " nodes connected");
    }

    private void handleLoginFailure(LoginResponseFailMessage failMessage) {
        if (loginFailCallback != null) {
            loginFailCallback.accept(failMessage);
        }
    }

    private void handleNodeConnect(NodeConnectMessage connectMessage) {
        RpcNodeInfo nodeInfo = connectMessage.nodeInfo;

        synchronized (remoteNodesLock) {
            remoteNodes.put(nodeInfo.id, new RpcRemoteNode(nodeInfo.id, nodeInfo.name, this));
        }

        if (nodeConnectCallback != null) {
            nodeConnectCallback.accept(null);
        }

        System.out.println("remote node " + nodeInfo.id + " (" + nodeInfo.name + ") connected");
    }

    private void handleNodeDisconnect(NodeDisconnectMessage disconnectMessage) {
        RpcNodeInfo nodeInfo = disconnectMessage.nodeInfo;

        synchronized (remoteNodesLock) {
            remoteNodes.remove(nodeInfo.id);
        }

        if (nodeDisconnectCallback != null) {
            nodeDisconnectCallback.accept(null);
        }

        System.out.println("remote node " + nodeInfo.id + " (" + nodeInfo.name + ") disconnected");
    }

    private void handleRequest(RpcRequestMessage requestMessage) {
        RpcLocalService localService = localServices.get(requestMessage.serviceId);

        RpcResult result = localService.handleRequest(requestMessage.methodId, requestMessage.bundle);

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

        synchronized (outstandingCallsLock) {
            future = outstandingCalls.remove(responseMessage.callId);
        }

        future.complete(responseMessage.result);
    }

    public RpcResult makeCall(int targetClient, int serviceId, int methodId, RpcBundle bundle) {
        RpcRequestMessage requestMessage = new RpcRequestMessage();
        requestMessage.sourceClient = 0xFF;
        requestMessage.targetClient = targetClient;
        requestMessage.serviceId = serviceId;
        requestMessage.methodId = methodId;
        requestMessage.bundle = bundle;

        CompletableFuture<RpcResult> future = new CompletableFuture<>();

        synchronized (outstandingCallsLock) {
            int callId = callCount++;

            requestMessage.callId = callId;

            outstandingCalls.put(callId, future);
        }

        try {
            synchronized (sendLock) {
                socketWrapper.sendMessage(requestMessage);
            }

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
        
        synchronized (remoteNodesLock) {
            copiedNodes = new HashMap<Integer, RpcRemoteNode>(remoteNodes);
        }

        return copiedNodes;
    }

    public boolean getIsConnected() {
        return isConnected;
    }
}
