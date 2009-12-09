package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

/**
 * @author Alexey Vladykin
 */
public class PushCommand implements PbCommand {

    @Override
    public String getName() {
        return "push";
    }

    @Override
    public String getDescription() {
        return "push changesets to parent repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.find(new File("."));

        String parentPath = repository.getParent();
        if (parentPath == null) {
            throw new PbException("No parent repository specified");
        }

        DiskRepository parent = DiskRepository.open(new File(parentPath));
        repository.push(parent);
    }
}