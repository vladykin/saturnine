package org.saturnine.cli.commands;

import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;
import org.saturnine.util.FileUtil;

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
        LocalRepository repository = LocalRepository.create(FileUtil.getCWD());
    }
}
