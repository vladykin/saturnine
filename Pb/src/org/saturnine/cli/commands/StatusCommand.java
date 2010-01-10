package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.api.Repository;
import org.saturnine.api.WorkDirState;
import org.saturnine.cli.PbCommand;
import org.saturnine.disk.impl.DiskRepository;

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
        Repository repository = DiskRepository.find(new File("."));
        WorkDirState workDirState = repository.getWorkDir().scanForChanges(null);
        for (String path : workDirState.getAddedFiles()) {
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
    }
}
