package org.saturnine.cli.commands;

import org.saturnine.cli.PbCommand;
import org.saturnine.api.PbException;

/**
 * @author Alexey Vladykin
 */
public class AddCommand implements PbCommand {

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "add file to repository";
    }

    @Override
    public void execute(String[] args) throws PbException {
        throw new PbException("Not supported yet.");
    }
}
