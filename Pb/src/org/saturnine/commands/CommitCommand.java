/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.commands;

import java.io.File;
import org.saturnine.api.PbException;
import org.saturnine.disk.impl.DiskRepository;

/**
 *
 * @author Alexey Vladykin
 */
public class CommitCommand implements Command {

    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.find(new File("."));
        repository.commit("Alexey", args[0]);
    }
}
