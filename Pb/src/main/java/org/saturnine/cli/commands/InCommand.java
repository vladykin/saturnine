package org.saturnine.cli.commands;

import org.saturnine.api.PbException;
import org.saturnine.cli.PbCommand;

/**
 * @author Alexey Vladykin
 */
public class InCommand implements PbCommand {

    @Override
    public String getName() {
        return "in";
    }

    @Override
    public String getDescription() {
        return "show incoming changesets";
    }

    @Override
    public void execute(String[] args) throws PbException {
        throw new PbException("Not supported yet.");
    }
}
