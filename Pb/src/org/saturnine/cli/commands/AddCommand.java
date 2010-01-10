package org.saturnine.cli.commands;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.saturnine.cli.PbCommand;
import org.saturnine.api.PbException;
import org.saturnine.api.WorkDir;
import org.saturnine.disk.impl.DiskRepository;

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
        DiskRepository repository = DiskRepository.find(new File("."));
        WorkDir workDir = repository.getWorkDir();
        workDir.add(Arrays.asList(args));
    }
}
