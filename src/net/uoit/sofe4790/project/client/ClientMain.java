package net.uoit.sofe4790.project.client;

import net.uoit.sofe4790.project.client.gui.LoginFrame;

import java.awt.*;
import java.io.IOException;

public class ClientMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClientHelper.instance.connect(args[0], 3500);

        EventQueue.invokeLater(() -> {
            try {
                LoginFrame frame = new LoginFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
