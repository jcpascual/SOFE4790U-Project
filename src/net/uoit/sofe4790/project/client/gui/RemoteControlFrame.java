package net.uoit.sofe4790.project.client.gui;

import net.uoit.sofe4790.project.client.ClientHelper;
import net.uoit.sofe4790.project.client.robot.IRobotService;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RemoteControlFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JLabel label;
    private JPanel contentPane;
    private boolean closing;

    /**
     * Create the frame.
     */
    public RemoteControlFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 960, 540);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        label = new JLabel();
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                IRobotService service = (IRobotService) ClientHelper.instance.getTargetService(IRobotService.SERVICE_ID);
                service.click(e.getX(), e.getY(), SwingUtilities.isRightMouseButton(e));
            }
        });

        JScrollPane scrollPane = new JScrollPane(label);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPane.add(scrollPane);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closing = true;
            }
        });

        closing = false;

        new Thread(this::screenUpdateThread).start();
    }

    private void screenUpdateThread() {
        IRobotService service = (IRobotService) ClientHelper.instance.getTargetService(IRobotService.SERVICE_ID);

        while (!closing) {
            try {
                byte[] screenshot = service.getScreenshot();

                label.setIcon(new ImageIcon(screenshot));

                Thread.sleep(500);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
