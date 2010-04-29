package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.DirState;
import org.saturnine.local.LocalRepository;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public class CopyCommand implements PbCommand {

    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public String getDescription() {
        return "copy a file";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        DirState dirstate = repository.getDirState();
        try {
            Utils.copyFiles(new File(repository.getPath(), args[0]), new File(repository.getPath(), args[1]));
        } catch (IOException ex) {
            throw new PbException("Failed to copy", ex);
        }
        try {
            DirState.Builder builder = dirstate.newBuilder(true);
            builder.addedFiles(Collections.singleton(args[1]));
            builder.origins(Collections.singletonMap(args[0], args[1]));
            builder.close();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }
}
