package org.saturnine.cli;

import java.util.List;
import org.saturnine.api.PbException;

/**
 * @author Alexey Vladykin
 */
public interface PbCommand {

    /**
     * @return full command name
     */
    String getName();

    /**
     * @return one-line description of the command
     */
    String getDescription();

    /**
     * Executes the command with given arguments.
     *
     * @param args  arguments specified after command name on the command line
     * @throws PbException  if something goes wrong
     */
    void execute(String[] args) throws PbException;
}
