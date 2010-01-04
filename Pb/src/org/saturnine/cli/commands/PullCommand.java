package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

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
        DiskRepository repository = DiskRepository.find(new File("."));

        String parentPath = repository.getProperty(DiskRepository.PROP_PARENT);
        if (parentPath == null) {
            throw new PbException("No parent repository specified");
        }

        DiskRepository parent = DiskRepository.open(new File(parentPath));
        repository.pull(parent);
    }
}
