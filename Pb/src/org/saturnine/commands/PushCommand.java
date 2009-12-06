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
public class PushCommand implements Command {

    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.find(new File("."));

        String parentPath = repository.getParent();
        if (parentPath == null) {
            throw new PbException("No parent repository specified");
        }

        DiskRepository parent = DiskRepository.open(new File(parentPath));
        repository.push(parent);
    }
}
