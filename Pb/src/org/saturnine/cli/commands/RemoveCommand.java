package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.DirState;
import org.saturnine.local.LocalRepository;
import org.saturnine.util.FileUtil;

/**
 * @author Alexey Vladykin
 */
public class RemoveCommand implements PbCommand {

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "remove a file";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        try {
            DirState.Builder builder = repository.getDirState().newBuilder(true);
            for (String path : args) {
                FileUtil.delete(new File(repository.getPath(), path));
                builder.removedFiles(Collections.singleton(path));
            }
            builder.close();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }
}
