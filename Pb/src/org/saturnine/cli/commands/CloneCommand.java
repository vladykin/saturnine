package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
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
        try {
            if (!source.getDirState().snapshot().scanDir().isClean()) {
                throw new PbException("Uncommitted changes in source repository");
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
        LocalRepository dest = LocalRepository.createClone(source, new File(args[1]));
    }
}
