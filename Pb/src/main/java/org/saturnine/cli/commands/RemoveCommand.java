package org.saturnine.cli.commands;

import java.io.IOException;
import java.util.Arrays;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;
import org.saturnine.local.WorkDir;
import org.saturnine.util.FileUtil;

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
        LocalRepository repository = LocalRepository.find(FileUtil.getCWD());
        WorkDir workdir = repository.getWorkDir();
        try {
            workdir.removeFiles(Arrays.asList(args));
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }
}
