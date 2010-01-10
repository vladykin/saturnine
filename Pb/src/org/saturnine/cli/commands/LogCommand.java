package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.Changeset;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;

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
        LocalRepository repository = LocalRepository.open(new File("."));
        String changesetID = repository.getHeads().iterator().next().getID();
        while (changesetID != null) {
            if (Changeset.NULL_ID.equals(changesetID)) {
                break;
            }

            Changeset changeset = repository.getChangeset(changesetID);
            if (changeset == null) {
                throw new PbException("Unable to find changeset " + changesetID);
            }
            System.out.println("changeset: " + changeset.getID());
            if (!changeset.getPrimaryParentID().equals(Changeset.NULL_ID)) {
                System.out.println("parent: " + changeset.getPrimaryParentID());
            }
            if (!changeset.getSecondaryParentID().equals(Changeset.NULL_ID)) {
                System.out.println("parent: " + changeset.getSecondaryParentID());
            }
            System.out.println("author: " + changeset.getAuthor());
            System.out.println("comment: " + changeset.getComment());
            System.out.println("timestamp: " + changeset.getTimestamp());
            System.out.println();

            changesetID = changeset.getPrimaryParentID();
        }
    }
}
