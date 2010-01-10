package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class CommitCommand implements PbCommand {

    @Override
    public String getName() {
        return "commit";
    }

    @Override
    public String getDescription() {
        return "commit changes";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        repository.getWorkDir().commit("Alexey", args[0], null);
    }
}
