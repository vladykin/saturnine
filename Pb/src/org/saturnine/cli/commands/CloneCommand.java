package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;

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
        LocalRepository source = LocalRepository.open(new File(args[0]));
        if (!source.getWorkDir().scanForChanges(null).isClean()) {
            throw new PbException("Uncommitted changes in source repository");
        }

        LocalRepository dest = LocalRepository.createClone(source, new File(args[1]));
    }
}
