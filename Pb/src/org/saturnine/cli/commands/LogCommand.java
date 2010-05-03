package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import org.saturnine.api.Changeset;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.Changelog;
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
        Changelog changelog = repository.getChangelog();
        try {
            Changelog.Reader reader = changelog.newReader();
            for (Changeset changeset = reader.next(); changeset != null; changeset = reader.next()) {
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
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }
}
