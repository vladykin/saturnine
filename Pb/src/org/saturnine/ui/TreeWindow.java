package org.saturnine.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import org.saturnine.api.WorkDir;
import org.saturnine.api.FileChange;
import org.saturnine.api.PbException;

/**
 *
 * @author Taisia
 */
public final class TreeWindow implements Runnable {
    public String caption;
    private WorkDir dirstate;
    private JTree tree;

    public TreeWindow()  {
        this.tree = new JTree(new DefaultMutableTreeNode());
    }

    public void run() {
        final JFrame form = new JFrame(caption);
        form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        form.getContentPane().setLayout(new FlowLayout());
        form.setMinimumSize(new Dimension(300, 200));

        JButton yesButton = new JButton("Approve");
         yesButton.setMaximumSize(new Dimension(40, 50));
         yesButton.setMinimumSize(new Dimension(40, 50));
         yesButton.setSize(40, 50);
         yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    dirstate.commit("Alexey", "Dummy message", null);
                    form.dispose();
                } catch (PbException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
         });


        JButton cancelButton = new JButton(" Cancel ");
         cancelButton.setMaximumSize(new Dimension(40, 50));
         cancelButton.setMinimumSize(new Dimension(40, 50));
         cancelButton.setSize(40, 50);
         cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                form.dispose();
            }
         });

        JPanel buttonpane = new JPanel();
        JPanel datapane = new JPanel();

        form.setLayout(new GridLayout(1,0));
        buttonpane.setLayout(new GridLayout(0, 1));
        datapane.setLayout(new GridLayout(1,1));
        JPanel yesPane = new JPanel();
         yesPane.add(yesButton);
         buttonpane.add(yesPane);
        JPanel cancelPane = new JPanel();
         cancelPane.add(cancelButton);
         buttonpane.add(cancelPane);

         JScrollPane scroll = new JScrollPane(tree);
         scroll.setPreferredSize(new Dimension(300, 200));
         datapane.add(scroll);


        form.add(datapane);
        form.add(buttonpane);

        form.update(null);
        form.pack();
        form.setLocationRelativeTo(null);
        form.setVisible(true);
    }

    void buildtree(WorkDir dirstate, Collection<FileChange> changes) {
        caption = "Check needed files";
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        root.setUserObject(dirstate.getRepository());
        for (FileChange change : changes) {
            String text = null;
            switch (change.getType()) {
                case ADD:
                    text = "Added " + change.getPath();
                    break;
                case MODIFY:
                    text = "Modified " + change.getPath();
                    break;
                case REMOVE:
                    text = "Removed " + change.getPath();
                    break;
            }
            root.add(new DefaultMutableTreeNode(text));
        }

        tree.expandRow(0);
    }

}
