package ltsa.ui;

/**
 * Created by mbrassesco on 6/23/17.
 */

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

public class FileBrowser implements Runnable {

    private DefaultMutableTreeNode root;

    private DefaultTreeModel treeModel;

    private JTree tree;

    public URL fileRoot;

    public String mySelection;

    public FileBrowser(URL examplesDir, String result) {
        fileRoot = examplesDir;
        mySelection = result;
    }


    @Override
    public void run() {
        JFrame frame = new JFrame("MTSA Examples");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        root = new DefaultMutableTreeNode(new FileNode(fileRoot));
        treeModel = new DefaultTreeModel(root);

        tree = new JTree(treeModel);

        //TODO: fix open the selected urlFile

        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                            tree.getLastSelectedPathComponent();
                    if (node == null) return;
                    mySelection = node.getUserObject().toString();
                    TreeNode[] path = node.getPath();
                    String plainPath = toPlainString(path);
                    if (mySelection.endsWith("lts"))
                        HPWindow.instance.newExample(plainPath, mySelection);
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        });

        tree.setShowsRootHandles(true);
        JScrollPane scrollPane = new JScrollPane(tree);

        frame.add(scrollPane);
        frame.setLocationByPlatform(true);
        frame.setSize(640, 480);

        CreateChildNodes ccn = new CreateChildNodes(fileRoot, root);
        new Thread(ccn).start();

        frame.setVisible(true);


    }

    private String toPlainString(TreeNode[] path) {
        String res = "";
        for (int i = 0; i < path.length - 1; i++) {
            res = res + String.valueOf(path[i]) + "/";
        }
        return res;
    }


    public class CreateChildNodes implements Runnable {

        private DefaultMutableTreeNode root;

        private URL fileRoot;

        public CreateChildNodes(URL fileRoot,
                                DefaultMutableTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
        }

        @Override
        public void run() {
            try {
                createChildren(fileRoot, root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void createChildren(URL fileRoot,
                                    DefaultMutableTreeNode node) throws IOException {
            URL[] urlFiles = (URL[]) fileRoot.getContent();
            if (urlFiles == null) return;

            for (URL urlFile : urlFiles) {
                DefaultMutableTreeNode childNode =
                        new DefaultMutableTreeNode(new FileNode(urlFile));
                node.add(childNode);
                if (urlFile.toExternalForm().endsWith("/")) {
                    createChildren(urlFile, childNode);
                }
            }
        }

    }

    public class FileNode {

        private URL urlFile;

        public FileNode(URL urlFile) {
            this.urlFile = urlFile;
        }

        @Override
        public String toString() {
            String name = urlFile.toExternalForm();
            if (name.equals("")) {
                return urlFile.getPath();
            } else {
                return name;
            }
        }
    }

}