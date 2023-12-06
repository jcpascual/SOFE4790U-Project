package net.uoit.sofe4790.project.server;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AuthorizedUsers {
    private Map<String, String> users;

    public AuthorizedUsers(String path) {
        users = new HashMap<>();

        // Write a file with testing credentials.
        File file = new File(path);
        if (!file.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
                writer.write("testuser;testpassword");
                writer.write('\n');
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Open a BufferedReader on this file.
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            // Read each line and split it over the semicolon.
            // The first element is the username, and the second element is the password.
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split(";");
                users.put(splitLine[0], splitLine[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkCredentials(String username, String password) {
        // Check if the username even exists in our list of credentials.
        if (!users.containsKey(username)) {
            return false;
        }

        // Check if the password associated with this username matches.
        String actualPassword = users.get(username);

        return actualPassword.equals(password);
    }
}
