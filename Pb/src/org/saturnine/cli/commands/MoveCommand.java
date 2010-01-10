package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.api.WorkDir;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

/**
 * @author Alexey Vladykin
 */
public class MoveCommand implements PbCommand {

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public String getDescription() {
        return "move a file";
    }

    @Override
    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.find(new File("."));
        WorkDir workDir = repository.getWorkDir();
        workDir.move(args[0], args[1]);
    }
}
