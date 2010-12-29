package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.LocalRepository;
import org.saturnine.util.FileUtil;

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
        LocalRepository repository = LocalRepository.find(FileUtil.getCWD());

        String parentPath = repository.getProperty(LocalRepository.PROP_PARENT);
        if (parentPath == null) {
            throw new PbException("No parent repository specified");
        }

        LocalRepository parent = LocalRepository.open(new File(parentPath));

        PullCommand.pull(repository, parent);
    }
}
