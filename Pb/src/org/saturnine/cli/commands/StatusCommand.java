package org.saturnine.cli.commands;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
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
            for (String path : sort(workDirState.getAddedFiles().keySet())) {
                System.out.println("A " + path);
            }
            for (String path : sort(workDirState.getRemovedFiles())) {
                System.out.println("R " + path);
            }
            for (String path : sort(workDirState.getMissingFiles())) {
                System.out.println("! " + path);
            }
            for (String path : sort(workDirState.getUntrackedFiles())) {
                System.out.println("? " + path);
            }
            for (String path : sort(workDirState.getModifiedFiles())) {
                System.out.println("M " + path);
            }
            for (String path : sort(workDirState.getUncertainFiles())) {
                System.out.println("?? " + path);
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    private static Collection<String> sort(Collection<String> orig) {
        return new TreeSet<String>(orig);
    }
}
