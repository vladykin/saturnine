package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.Changeset;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

/**
 * @author Alexey Vladykin
 */
public class LogCommand implements PbCommand {

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public String getDescription() {
        return "show changesets";
    }

    @Override
    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.open(new File("."));
        String changesetID = repository.getHeadIDs().iterator().next();
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

            changesetID = changeset.getParentIDs().iterator().next();
        }
    }
}
