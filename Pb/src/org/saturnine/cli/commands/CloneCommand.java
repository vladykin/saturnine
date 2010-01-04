package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

/**
 * @author Alexey Vladykin
 */
public class CloneCommand implements PbCommand {

    @Override
    public String getName() {
        return "clone";
    }

    @Override
    public String getDescription() {
        return "create a local copy of repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        DiskRepository source = DiskRepository.open(new File(args[0]));
        if (!source.getDirState().getWorkDirChanges(null).isEmpty()) {
            throw new PbException("Uncommitted changes in source repository");
        }

        DiskRepository dest = DiskRepository.createClone(source, new File(args[1]));
    }
}
