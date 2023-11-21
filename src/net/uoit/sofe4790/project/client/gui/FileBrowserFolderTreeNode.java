package net.uoit.sofe4790.project.client.gui;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileBrowserFolderTreeNode extends DefaultMutableTreeNode {
    public FileBrowserFolderTreeNode(String str) {
        super(str);
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
