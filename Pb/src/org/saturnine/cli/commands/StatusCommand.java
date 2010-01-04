package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.DirState;
import org.saturnine.api.FileChange;
import org.saturnine.api.PbException;
import org.saturnine.api.Repository;
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
        DirState dirstate = repository.getDirState();
        for (FileChange change : dirstate.getWorkDirChanges(null)) {
            switch (change.getType()) {
                case ADD:
                    printAdd(change.getPath(), dirstate);
                    break;
                case MODIFY:
                    printModify(change.getPath(), dirstate);
                    break;
                case REMOVE:
                    printRemove(change.getPath(), dirstate);
                    break;
                default:
                    throw new PbException("Unknown change type " + change.getType());
            }
        }
    }

    private void printAdd(String path, DirState dirstate) throws PbException {
        String code = dirstate.isAboutToAdd(path)? "A   " : "?   ";
        System.out.println(code + path);
    }

    private void printModify(String path, DirState dirstate) throws PbException {
        System.out.println("M   " + path);
    }

    private void printRemove(String path, DirState dirstate) throws PbException {
        String code = dirstate.isAboutToRemove(path)? "R   " : "!   ";
        System.out.println(code + path);
    }
}
