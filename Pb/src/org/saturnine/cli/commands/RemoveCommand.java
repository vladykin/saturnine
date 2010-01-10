package org.saturnine.cli.commands;

import java.io.File;
import java.util.Arrays;
import org.saturnine.api.PbException;
import org.saturnine.api.WorkDir;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class RemoveCommand implements PbCommand {

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "remove a file";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        WorkDir workDir = repository.getWorkDir();
        workDir.remove(Arrays.asList(args));
    }
}
