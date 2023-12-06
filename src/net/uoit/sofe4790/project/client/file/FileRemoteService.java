package net.uoit.sofe4790.project.client.file;

import net.uoit.sofe4790.project.client.rpc.RpcClient;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteService;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

import java.io.IOException;

public class FileRemoteService extends RpcRemoteService implements IFileService {
    public FileRemoteService(int target, RpcClient client) {
        super(target, client);
    }

    @Override
    public String[] getFilesInDirectory(String path) {
        RpcBundle bundle = new RpcBundle();
        bundle.putString("path", path);

        RpcResult result = client.makeCall(target, SERVICE_ID, METHOD_ID_GET_FILES_IN_DIRECTORY, bundle);

        return result.bundle.getStringArray(RETURN_VALUE);
    }

    @Override
    public String[] getDirectoriesInDirectory(String path) {
        RpcBundle bundle = new RpcBundle();
        bundle.putString("path", path);

        RpcResult result = client.makeCall(target, SERVICE_ID, METHOD_ID_GET_DIRECTORIES_IN_DIRECTORY, bundle);

        return result.bundle.getStringArray(RETURN_VALUE);
    }

    @Override
    public byte[] getFile(String path) throws IOException {
        RpcBundle bundle = new RpcBundle();
        bundle.putString("path", path);

        RpcResult result = client.makeCall(target, SERVICE_ID, METHOD_ID_GET_FILE, bundle);

        return result.bundle.getByteArray(RETURN_VALUE);
    }

    @Override
    public void putFile(String path, byte[] data) throws IOException {
        RpcBundle bundle = new RpcBundle();
        bundle.putString("path", path);
        bundle.putByteArray("data", data);

        client.makeCall(target, SERVICE_ID, METHOD_ID_PUT_FILE, bundle);
    }

    @Override
    public void makeFolder(String path) throws IOException {
        RpcBundle bundle = new RpcBundle();
        bundle.putString("path", path);

        client.makeCall(target, SERVICE_ID, METHOD_ID_CREATE_FOLDER, bundle);
    }
}
