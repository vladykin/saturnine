package org.saturnine.cli.commands;

import java.io.File;
import java.util.Arrays;
import org.saturnine.cli.PbCommand;
import org.saturnine.api.PbException;
import org.saturnine.api.WorkDir;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class AddCommand implements PbCommand {

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "add file to repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        WorkDir workDir = repository.getWorkDir();
        workDir.add(Arrays.asList(args));
    }
}
