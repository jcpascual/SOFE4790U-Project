package net.uoit.sofe4790.project.client;

import net.uoit.sofe4790.project.client.gui.LoginFrame;

import java.awt.*;
import java.io.IOException;

public class ClientDebugMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClientHelper.instance.connect(args[0], 3500);

        ClientHelper.instance.login("testuser", "testpassword", args[1]);

        while (true) {
            Thread.sleep(500);
        }
    }
}
