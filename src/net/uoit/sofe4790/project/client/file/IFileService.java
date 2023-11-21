package net.uoit.sofe4790.project.client.file;

import java.io.IOException;

public interface IFileService {
    int SERVICE_ID = 2;

    int METHOD_ID_GET_FILES_IN_DIRECTORY = 1;
    int METHOD_ID_GET_DIRECTORIES_IN_DIRECTORY = 2;
    int METHOD_ID_GET_FILE = 3;
    int METHOD_ID_PUT_FILE = 4;

    String RETURN_VALUE = "ret";

    String[] getFilesInDirectory(String path);

    String[] getDirectoriesInDirectory(String path);

    byte[] getFile(String path) throws IOException;

    void putFile(String parentPath, byte[] data) throws IOException;
}
