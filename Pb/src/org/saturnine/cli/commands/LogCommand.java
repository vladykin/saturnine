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
        String changesetID = repository.getHeads().iterator().next().id();
        while (changesetID != null) {
            if (Changeset.NULL.equals(changesetID)) {
                break;
            }

            Changeset changeset = repository.getChangeset(changesetID);
            if (changeset == null) {
                throw new PbException("Unable to find changeset " + changesetID);
            }
            System.out.println("changeset: " + changeset.id());
            if (!changeset.primaryParent().equals(Changeset.NULL)) {
                System.out.println("parent: " + changeset.primaryParent());
            }
            if (!changeset.secondaryParent().equals(Changeset.NULL)) {
                System.out.println("parent: " + changeset.secondaryParent());
            }
            System.out.println("author: " + changeset.author());
            System.out.println("comment: " + changeset.comment());
            System.out.println("timestamp: " + changeset.timestamp());
            System.out.println();

            changesetID = changeset.primaryParent();
        }
    }
}
