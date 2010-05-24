package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;
import org.saturnine.local.DirScanResult;
import org.saturnine.local.LocalRepository;

/**
 * @author Alexey Vladykin
 */
public class StatusCommand implements PbCommand {

    @Override
    public String getName() {
        return "status";
    }

    @Override
    public String getDescription() {
        return "show uncommitted changes";
    }

    @Override
    public void execute(String[] args) throws PbException {
        LocalRepository repository = LocalRepository.find(new File("."));
        try {
            DirScanResult workDirState = repository.getWorkDir().scan();
            for (String path : workDirState.getAddedFiles().keySet()) {
                System.out.println("A " + path);
            }
            for (String path : workDirState.getRemovedFiles()) {
                System.out.println("R " + path);
            }
            for (String path : workDirState.getMissingFiles()) {
                System.out.println("! " + path);
            }
            for (String path : workDirState.getUntrackedFiles()) {
                System.out.println("? " + path);
            }
            for (String path : workDirState.getModifiedFiles()) {
                System.out.println("M " + path);
            }
            for (String path : workDirState.getUncertainFiles()) {
                System.out.println("M " + path);
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }
}
