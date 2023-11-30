package net.uoit.sofe4790.project.client.gui;

import net.uoit.sofe4790.project.client.ClientHelper;
import net.uoit.sofe4790.project.client.rpc.RpcClient;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteNode;
import net.uoit.sofe4790.project.common.rpc.RpcNodeInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class NodeChooserFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JComboBox<String> comboBoxNodes;

    /**
     * Create the frame.
     */
    public NodeChooserFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 220, 140);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        comboBoxNodes = new JComboBox<>();
        comboBoxNodes.setBounds(6, 6, 208, 27);
        comboBoxNodes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = comboBoxNodes.getSelectedIndex();

                // Change the target node if an item is selected.
                if (index != -1) {
                    for (RpcRemoteNode node : ClientHelper.instance.getNodes().values()) {
                        if (node.name.equals(comboBoxNodes.getSelectedItem())) {
                            ClientHelper.instance.setTargetNode(node.id);
                            break;
                        }
                    }
                }
            }
        });
        contentPane.add(comboBoxNodes);

        JButton btnFileTransfer = new JButton("File Transfer");
        btnFileTransfer.setBounds(6, 35, 208, 29);
        btnFileTransfer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the file browser if a node is selected.
                if (comboBoxNodes.getSelectedIndex() != -1) {
                    FileBrowserFrame frame = new FileBrowserFrame();
                    frame.setVisible(true);
                } else {
                    showNodeSelectionError();
                }
            }
        });
        contentPane.add(btnFileTransfer);

        JButton btnRemoteControl = new JButton("Remote Control");
        btnRemoteControl.setBounds(6, 68, 208, 29);
        btnRemoteControl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open the remote control if a node is selected.
                if (comboBoxNodes.getSelectedIndex() != -1) {
                    RemoteControlFrame frame = new RemoteControlFrame();
                    frame.setVisible(true);
                } else {
                    showNodeSelectionError();
                }
            }
        });
        contentPane.add(btnRemoteControl);

        // Register connect and disconnect callbacks.
        RpcClient client = ClientHelper.instance.getClient();
        client.nodeConnectCallback = this::nodeConnect;
        client.nodeDisconnectCallback = this::nodeDisconnect;

        // Refresh the combo box's contents.
        refreshComboBox();
    }

    private void refreshComboBox() {
        // Remove all items from the combo box.
        comboBoxNodes.removeAllItems();

        int targetNode = ClientHelper.instance.getTargetNode();
        int targetNodeIdx = -1;

        HashMap<Integer, RpcRemoteNode> nodes = ClientHelper.instance.getNodes();
        for (RpcRemoteNode node : nodes.values()) {
            if (node.id == targetNode) {
                targetNodeIdx = comboBoxNodes.getItemCount();
            }

            comboBoxNodes.addItem(node.name);
        }

        comboBoxNodes.setSelectedIndex(targetNodeIdx);
    }

    private void nodeConnect(RpcNodeInfo nodeInfo) {
        refreshComboBox();
    }

    private void nodeDisconnect(RpcNodeInfo nodeInfo) {
        refreshComboBox();
    }

    private void showNodeSelectionError() {
        JOptionPane.showMessageDialog(this, "No computer selected.", "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
