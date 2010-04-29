package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.saturnine.cli.PbCommand;
import org.saturnine.api.PbException;
import org.saturnine.local.DirState;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class AddCommand implements PbCommand {

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "add file to repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        DirState dirstate = repository.getDirState();
        try {
            DirState.Builder builder = dirstate.newBuilder(true);
            builder.addedFiles(new HashSet<String>(Arrays.asList(args)));
            builder.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }
}
