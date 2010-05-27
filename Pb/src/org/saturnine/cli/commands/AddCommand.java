package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.saturnine.cli.PbCommand;
import org.saturnine.api.PbException;
import org.saturnine.local.LocalRepository;
import org.saturnine.local.WorkDir;

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
        WorkDir workdir = repository.getWorkDir();
        try {
            workdir.addFiles(0 < args.length? Arrays.asList(args) : Collections.singleton(""));
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }
}
