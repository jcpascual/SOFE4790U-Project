package net.uoit.sofe4790.project.client.gui;

import net.uoit.sofe4790.project.client.ClientHelper;
import net.uoit.sofe4790.project.client.rpc.RpcClient;
import net.uoit.sofe4790.project.common.message.LoginResponseFailMessage;
import net.uoit.sofe4790.project.common.message.LoginResponseSuccessMessage;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textFieldUsername;
    private JTextField textFieldPassword;
    private JTextField textFieldClientName;
    private JButton btnLogin;

    /**
     * Create the frame.
     */
    public LoginFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 225, 150);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        textFieldUsername = new JTextField();
        textFieldUsername.setBounds(79, 6, 130, 26);
        contentPane.add(textFieldUsername);
        textFieldUsername.setColumns(10);

        JLabel lblUsername = new JLabel("Username");
        lblUsername.setBounds(6, 11, 72, 16);
        contentPane.add(lblUsername);

        JLabel lblPassword = new JLabel("Password");
        lblPassword.setBounds(6, 39, 61, 16);
        contentPane.add(lblPassword);

        textFieldPassword = new JTextField();
        textFieldPassword.setBounds(79, 34, 130, 26);
        contentPane.add(textFieldPassword);
        textFieldPassword.setColumns(10);

        JLabel lblClientName = new JLabel("Name");
        lblClientName.setBounds(6, 67, 61, 16);
        contentPane.add(lblClientName);

        textFieldClientName = new JTextField();
        textFieldClientName.setBounds(79, 61, 130, 26);
        contentPane.add(textFieldClientName);
        textFieldClientName.setColumns(10);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(92, 87, 117, 29);
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnLogin.setEnabled(false);

                try {
                    ClientHelper.instance.getClient().login(textFieldUsername.getText(), textFieldPassword.getText(), textFieldClientName.getText());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        btnLogin.setEnabled(true);
        contentPane.add(btnLogin);

        RpcClient client = ClientHelper.instance.getClient();
        client.loginSuccessCallback = this::loginSuccess;
        client.loginFailCallback = this::loginFail;
    }

    private void loginSuccess(LoginResponseSuccessMessage successMessage) {
        EventQueue.invokeLater(() -> {
            RpcClient client = ClientHelper.instance.getClient();
            client.loginSuccessCallback = null;
            client.loginFailCallback = null;

            setVisible(false);
            dispose();

            NodeChooserFrame frame = new NodeChooserFrame();
            frame.setVisible(true);
        });
    }

    private void loginFail(LoginResponseFailMessage failMessage) {
        JOptionPane.showMessageDialog(this, "Invalid username or password", "Error",
                JOptionPane.ERROR_MESSAGE);
        btnLogin.setEnabled(true);
    }
}
