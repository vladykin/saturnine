package org.saturnine.cli.commands;

import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;

/**
 * @author Alexey Vladykin
 */
public class MoveCommand implements PbCommand {

    @Override
    public String getName() {
        return "move";
    }

    @Override
    public String getDescription() {
        return "move a file";
    }

    @Override
    public void execute(String[] args) throws PbException {
        throw new PbException("Not supported yet.");
    }
}
