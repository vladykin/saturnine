/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saturnine.commands;

import java.io.File;
import org.saturnine.api.Changeset;
import org.saturnine.api.PbException;
import org.saturnine.disk.impl.DiskRepository;

/**
 *
 * @author Alexey Vladykin
 */
public class LogCommand implements Command {

    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.open(new File("."));
        String changesetID = repository.getCurrentID();
        while (changesetID != null) {
            if (Changeset.NULL_ID.equals(changesetID)) {
                break;
            }

            Changeset changeset = repository.getChangeset(changesetID);
            if (changeset == null) {
                throw new PbException("Unable to find changeset " + changesetID);
            }
            System.out.println("changeset: " + changeset.getID());
            for (String parentID : changeset.getParentIDs()) {
                System.out.println("parent: " + parentID);
            }
            System.out.println("author: " + changeset.getAuthor());
            System.out.println("comment: " + changeset.getComment());
            System.out.println("timestamp: " + changeset.getTimestamp());
            System.out.println();

            changesetID = changeset.getParentIDs().get(0);
        }
    }
}
