package org.saturnine.cli.commands;

import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;

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
        throw new PbException("Not supported yet.");
    }
}
