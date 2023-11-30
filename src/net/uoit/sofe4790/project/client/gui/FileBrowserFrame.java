package net.uoit.sofe4790.project.client.gui;

import net.uoit.sofe4790.project.client.ClientHelper;
import net.uoit.sofe4790.project.client.file.IFileService;
import net.uoit.sofe4790.project.client.rpc.RpcRemoteNode;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

public class FileBrowserFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTree tree;
    private JPopupMenu popupMenuFolder;
    private JPopupMenu popupMenuFile;

    private IFileService fileService;

    /**
     * Create the frame.
     */
    public FileBrowserFrame() {
        fileService = (IFileService) ClientHelper.instance.getTargetService(IFileService.SERVICE_ID);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        setContentPane(contentPane);
        contentPane.setLayout(null);

        tree = new JTree();
        tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath treePath = event.getPath();
                String fsPath = createFileSystemPathFromTreePath(treePath);

                addChildrenToNode((DefaultMutableTreeNode) treePath.getLastPathComponent(), fsPath);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // We don't care about this.
            }
        });
        tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                int x = e.getX();
                int y = e.getY();

                int row = tree.getClosestRowForLocation(x, y);

                if (row == -1) {
                    return;
                }

                tree.setSelectionRow(row);

                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();

                if (treeNode instanceof FileBrowserFolderTreeNode) {
                    popupMenuFolder.show(tree, x, y);
                } else {
                    popupMenuFile.show(tree, x, y);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // We don't care about this.
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // We don't care about this.
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // We don't care about this.
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // We don't care about this.
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(6, 6, 438, 226);
        contentPane.add(scrollPane);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(6, 237, 117, 29);
        btnRefresh.addActionListener(e -> refresh());
        contentPane.add(btnRefresh);

        popupMenuFolder = new JPopupMenu();
        popupMenuFolder.setBounds(0, 0, 161, 27);
        contentPane.add(popupMenuFolder);

        JMenuItem mntmUpload = new JMenuItem("Upload Here");
        mntmUpload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                int result = fileChooser.showOpenDialog(FileBrowserFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    TreePath selectedPath = tree.getSelectionPath();
                    String selectedFsPath = createFileSystemPathFromTreePath(selectedPath);

                    try {
                        byte[] data = Files.readAllBytes(Path.of(file.getPath()));
                        fileService.putFile(selectedFsPath + file.getName(), data);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        popupMenuFolder.add(mntmUpload);

        popupMenuFile = new JPopupMenu();
        popupMenuFile.setBounds(0, 0, 161, 27);
        contentPane.add(popupMenuFile);

        JMenuItem mntmDownload = new JMenuItem("Download");
        mntmDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();

                int result = fileChooser.showSaveDialog(FileBrowserFrame.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    TreePath selectedPath = tree.getSelectionPath();
                    String selectedFsPath = createFileSystemPathFromTreePath(selectedPath);

                    try {
                        byte[] data = fileService.getFile(selectedFsPath);
                        Files.write(Path.of(file.getPath()), data, StandardOpenOption.CREATE);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        popupMenuFile.add(mntmDownload);

        refresh();
    }

    private void addChildrenToNode(DefaultMutableTreeNode parentTreeNode, String path) {
        parentTreeNode.removeAllChildren();

        String[] directories = fileService.getDirectoriesInDirectory(path);

        for (String str : directories) {
            parentTreeNode.add(new FileBrowserFolderTreeNode(str));
        }

        String[] files = fileService.getFilesInDirectory(path);

        for (String str : files) {
            parentTreeNode.add(new DefaultMutableTreeNode(str));
        }
    }

    private void refresh() {
        DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode("/");

        addChildrenToNode(rootTreeNode, "/");

        DefaultTreeModel treeModel = new DefaultTreeModel(rootTreeNode);

        tree.setModel(treeModel);
    }

    private String createFileSystemPathFromTreePath(TreePath treePath) {
        StringBuilder builder = new StringBuilder();
        builder.append('/');

        for (Object obj : treePath.getPath()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)obj;

            if (node.isRoot()) {
                continue;
            }

            builder.append((String)node.getUserObject());
            builder.append('/');
        }

        return builder.toString();
    }
}
