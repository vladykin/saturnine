package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

/**
 * @author Alexey Vladykin
 */
public class InitCommand implements PbCommand{

    @Override
    public String getName() {
        return "init";
    }

    @Override
    public String getDescription() {
        return "initialize a new repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.create(new File("."));
    }
}
