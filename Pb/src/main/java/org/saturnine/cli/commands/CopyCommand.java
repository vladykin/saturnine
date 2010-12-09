package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;
import org.saturnine.local.WorkDir;

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
        WorkDir workdir = repository.getWorkDir();
        try {
            workdir.copyFiles(Arrays.asList(args));
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }
}
