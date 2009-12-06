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
public class CloneCommand implements Command {

    public void execute(String[] args) throws PbException {
        DiskRepository source = DiskRepository.open(new File(args[0]));
        if (!source.status().isEmpty()) {
            throw new PbException("Uncommitted changes in source repository");
        }

        DiskRepository dest = DiskRepository.createClone(source, new File(args[1]));
    }
}
