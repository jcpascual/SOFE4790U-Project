package net.uoit.sofe4790.project.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AuthorizedUsers {
    private Map<String, String> users;

    public AuthorizedUsers(String path) {
        users = new HashMap<>();

        File file = new File(path);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                writer.write("testuser;testpassword");
                writer.write('\n');
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(";");
                users.put(splitLine[0], splitLine[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkCredentials(String username, String password) {
        if (!users.containsKey(username)) {
            return false;
        }

        String actualPassword = users.get(username);

        return actualPassword.equals(password);
    }
}
