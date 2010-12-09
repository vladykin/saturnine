package org.saturnine.cli.commands;

import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;

/**
 * @author Alexey Vladykin
 */
public class OutCommand implements PbCommand {

    @Override
    public String getName() {
        return "out";
    }

    @Override
    public String getDescription() {
        return "show outgoing changesets";
    }

    @Override
    public void execute(String[] args) throws PbException {
        throw new PbException("Not supported yet.");
    }
}
