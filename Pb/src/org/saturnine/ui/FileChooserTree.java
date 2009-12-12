/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.ui;

import java.awt.event.ActionEvent;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.*;
import org.saturnine.api.PbException;
import org.saturnine.api.FileChange;
import org.saturnine.disk.impl.DiskRepository;
/**
 *
 * @author Taisia
 */
public final class FileChooserTree implements Runnable {

    public String caption;
    public boolean isonefile;
    public String isonefilename;
    private DiskRepository repository;
    private List<FileChange> changes;

    public void run() {
        //throw new UnsupportedOperationException("Not supported yet.");

        //void edittree(){

        final JFrame form = new JFrame(caption);
        form.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        form.getContentPane().setLayout(new FlowLayout());
        form.setMinimumSize(new Dimension(400, 100));

        JButton yesButton = new JButton("   Yes   ");
         yesButton.setMaximumSize(new Dimension(40, 50));
         yesButton.setMinimumSize(new Dimension(40, 50));
         yesButton.setSize(40, 50);
         yesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    repository.commit("Alexey", "Dummy message", null);
                    form.dispose();
                } catch (PbException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
         });

        JButton noButton = new JButton("    No    ");
         noButton.setMaximumSize(new Dimension(40, 50));
         noButton.setMinimumSize(new Dimension(40, 50));
         noButton.setSize(40, 50);
         noButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {                
                   TreeWindow OpenTree = new TreeWindow();
                   //TODO: dont forget to change input data
                   OpenTree.buildtree(repository, changes);
                   SwingUtilities.invokeLater(OpenTree);
                   //form.setVisible(false);
                   form.dispose();
                }
         });

        JButton cancelButton = new JButton("Cancel");
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

        form.setLayout(new GridLayout(0,1));

        if (isonefile) {
            datapane.add(new JLabel("Would you like to commit file \""+isonefilename+"\"?"));
            buttonpane.add(yesButton);
            buttonpane.add(noButton);
        } else {
            //form.setLayout(new GridLayout(1,0));
            //buttonpane.setLayout(new GridLayout(0, 1));
            datapane.add(new JLabel("Would you like to commit ALL changed files?"));
            buttonpane.add(yesButton);
            buttonpane.add(noButton);
            buttonpane.add(cancelButton);

            /*
            JPanel yesPane = new JPanel();
             yesPane.add(yesButton);
             buttonpane.add(yesPane);
            
            JPanel noPane = new JPanel();
             noPane.add(noButton);
             buttonpane.add(noPane);
            
            JPanel cancelPane = new JPanel();
             cancelPane.add(cancelButton);
             buttonpane.add(cancelPane);
            */
        }

        form.add(datapane);
        form.add(buttonpane);

        form.update(null);
        form.pack();
        form.setLocationRelativeTo(null);
        form.setVisible(true);
    }

    public void createTree(DiskRepository repository, List<FileChange> changes) {
        this.repository = repository;
        this.changes = changes;
        caption = "Choose files to be committed";
        isonefile = changes.size() == 1;
        if (isonefile) {
            isonefilename = changes.get(0).getPath();
        }
    }

    public static void main(String[] args) throws Exception {

        DiskRepository repository = DiskRepository.open(new File(args[0]));
        List<FileChange> changes = repository.getWorkDirChanges(null);

        FileChooserTree window = new FileChooserTree();
        window.createTree(repository, changes);
        SwingUtilities.invokeLater(window);
    }
}
