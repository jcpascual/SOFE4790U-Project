package net.uoit.sofe4790.project.client.file;

import net.uoit.sofe4790.project.client.rpc.RpcLocalService;
import net.uoit.sofe4790.project.common.rpc.RpcBundle;
import net.uoit.sofe4790.project.common.rpc.RpcResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileLocalService extends RpcLocalService implements IFileService {
    @Override
    public String[] getFilesInDirectory(String path) {
        File file = new File(path);
        File[] contents = file.listFiles();

        assert contents != null;

        List<String> returnContents = new ArrayList<>();

        for (int i = 0; i < contents.length; i++) {
            File content = contents[i];

            if (content.isDirectory()) {
                continue;
            }

            returnContents.add(content.getName());
        }

        return returnContents.toArray(new String[0]);
    }

    @Override
    public String[] getDirectoriesInDirectory(String path) {
        File file = new File(path);
        File[] contents = file.listFiles();

        assert contents != null;

        List<String> returnContents = new ArrayList<>();

        for (int i = 0; i < contents.length; i++) {
            File content = contents[i];

            if (content.isFile()) {
                continue;
            }

            returnContents.add(content.getName());
        }

        return returnContents.toArray(new String[0]);
    }

    @Override
    public byte[] getFile(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }

    @Override
    public void putFile(String path, byte[] data) throws IOException {
        Files.write(Path.of(path), data, StandardOpenOption.CREATE);
    }

    @Override
    public RpcResult handleRequest(int methodId, RpcBundle bundle) {
        RpcResult result = new RpcResult();
        result.success = true;

        if (methodId == METHOD_ID_GET_FILES_IN_DIRECTORY) {
            String path = bundle.getString("path");
            String[] returnValue = getFilesInDirectory(path);

            result.bundle.putStringArray(RETURN_VALUE, returnValue);
        } else if (methodId == METHOD_ID_GET_DIRECTORIES_IN_DIRECTORY) {
            String path = bundle.getString("path");
            String[] returnValue = getDirectoriesInDirectory(path);

            result.bundle.putStringArray(RETURN_VALUE, returnValue);
        } else if (methodId == METHOD_ID_GET_FILE) {
            String path = bundle.getString("path");

            byte[] returnValue;
            try {
                returnValue = getFile(path);

                result.bundle.putByteArray(RETURN_VALUE, returnValue);
            } catch (IOException e) {
                result.success = false;

                e.printStackTrace();
            }
        } else if (methodId == METHOD_ID_PUT_FILE) {
            String path = bundle.getString("path");
            byte[] data = bundle.getByteArray("data");

            try {
                putFile(path, data);
            } catch (IOException e) {
                result.success = false;

                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Invalid method ID");
        }

        return result;
    }
}
