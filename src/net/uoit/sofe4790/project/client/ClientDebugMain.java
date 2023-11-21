package net.uoit.sofe4790.project.client;

import net.uoit.sofe4790.project.client.gui.LoginFrame;

import java.awt.*;
import java.io.IOException;

public class ClientDebugMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClientHelper.instance.connect("127.0.0.1", 3500, "client");

        ClientHelper.instance.login("testuser", "testpassword", "Remote Computer A");

        while (true) {
            Thread.sleep(500);
        }
    }
}
