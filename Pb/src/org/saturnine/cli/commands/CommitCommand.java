package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

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
        DiskRepository repository = DiskRepository.find(new File("."));
        repository.commit("Alexey", args[0]);
    }
}
