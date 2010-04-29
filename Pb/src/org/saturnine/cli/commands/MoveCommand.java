package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.DirState;
import org.saturnine.local.LocalRepository;

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
        LocalRepository repository = LocalRepository.find(new File("."));
        boolean success = new File(repository.getPath(), args[0]).renameTo(
                new File(repository.getPath(), args[1]));
        if (!success) {
            throw new PbException("Failed to move " + args[0] + " to " + args[1]);
        }
        try {
            DirState.Builder builder = repository.getDirState().newBuilder(true);
            builder.removedFiles(Collections.singleton(args[0]));
            builder.addedFiles(Collections.singleton(args[1]));
            builder.origins(Collections.singletonMap(args[0], args[1]));
            builder.close();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }
}
