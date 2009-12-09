package org.saturnine.cli.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.api.UncommittedFileChange;
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
        DiskRepository repository = DiskRepository.find(new File("."));
        for (UncommittedFileChange change : repository.status()) {
            switch (change.getType()) {
                case ADD:
                    System.out.println((change.isApproved() ? "A   " : "?   ") + change.getResultState().getPath());
                    break;
                case MODIFY:
                    System.out.println("M   " + change.getResultState().getPath());
                    break;
                case MOVE:
                case MOVE_MODIFY:
                    System.out.println("R   " + change.getOriginalState().getPath());
                    System.out.println("A   " + change.getResultState().getPath());
                    break;
                case REMOVE:
                    System.out.println((change.isApproved() ? "R   " : "!   ") + change.getOriginalState().getPath());
                    break;
                default:
                    throw new PbException("Unknown change type " + change.getType());
            }
        }
    }
}
