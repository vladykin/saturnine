package org.saturnine.commands;

import org.saturnine.api.PbException;

/**
 *
 * @author Alexey Vladykin
 */
public interface Command {
    void execute(String[] args) throws PbException;
}
