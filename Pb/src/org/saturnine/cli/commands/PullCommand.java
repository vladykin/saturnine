package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class PullCommand implements PbCommand {

    @Override
    public String getName() {
        return "pull";
    }

    @Override
    public String getDescription() {
        return "pull changesets from parent repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));

        String parentPath = repository.getProperty(LocalRepository.PROP_PARENT);
        if (parentPath == null) {
            throw new PbException("No parent repository specified");
        }

        LocalRepository parent = LocalRepository.open(new File(parentPath));
        repository.pull(parent);
    }
}
