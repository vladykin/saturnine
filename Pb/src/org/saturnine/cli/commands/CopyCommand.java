package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.api.WorkDir;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class CopyCommand implements PbCommand {

    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public String getDescription() {
        return "copy a file";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        WorkDir workDir = repository.getWorkDir();
        workDir.copy(args[0], args[1]);
    }
}
