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
    private String performPathCorrectionForWindows(String path) {
        // Get the OS name.
        String osName = System.getProperty("os.name");

        // If not Windows, just return the path.
        if (!osName.contains("Windows")) {
            return path;
        }

        // If it is Windows, use backslashes as the path separator and add a drive letter.
        return "C:" + path.replace('/', '\\');
    }

    @Override
    public String[] getFilesInDirectory(String path) {
        path = performPathCorrectionForWindows(path);

        // Get all files in this path.
        File file = new File(path);
        File[] contents = file.listFiles();

        // If empty, return a zero-length array.
        if (contents == null) {
            return new String[0];
        }

        List<String> returnContents = new ArrayList<>();

        // Record all the regular files.
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
        path = performPathCorrectionForWindows(path);

        // Get all files in this path.
        File file = new File(path);
        File[] contents = file.listFiles();

        // If empty, return a zero-length array.
        if (contents == null) {
            return new String[0];
        }

        List<String> returnContents = new ArrayList<>();

        // Record all the directories.
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
        path = performPathCorrectionForWindows(path);

        // Return all bytes of the file in this path.
        return Files.readAllBytes(Path.of(path));
    }

    @Override
    public void putFile(String path, byte[] data) throws IOException {
        path = performPathCorrectionForWindows(path);

        // Write the byte array to a new file at this path.
        Files.write(Path.of(path), data, StandardOpenOption.CREATE);
    }

    @Override
    public void makeFolder(String path) throws IOException {
        path = performPathCorrectionForWindows(path);

        // Create a folder at this path.
        Files.createDirectory(Path.of(path));
    }

    @Override
    public RpcResult handleRequest(int methodId, RpcBundle bundle) {
        RpcResult result = new RpcResult();
        result.success = true;

        if (methodId == METHOD_ID_GET_FILES_IN_DIRECTORY) {
            // Get all the files in the requested path and put it in the return bundle.
            String path = bundle.getString("path");
            String[] returnValue = getFilesInDirectory(path);

            result.bundle.putStringArray(RETURN_VALUE, returnValue);
        } else if (methodId == METHOD_ID_GET_DIRECTORIES_IN_DIRECTORY) {
            // Get all the directories in the requested path and put it in the return bundle.
            String path = bundle.getString("path");
            String[] returnValue = getDirectoriesInDirectory(path);

            result.bundle.putStringArray(RETURN_VALUE, returnValue);
        } else if (methodId == METHOD_ID_GET_FILE) {
            String path = bundle.getString("path");

            // Get the file's data and store it in the return bundle.
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

            // Store the requested data into the requested path.
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
