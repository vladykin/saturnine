/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.commands;

import org.saturnine.api.PbException;
import java.io.File;
import org.saturnine.disk.impl.DiskRepository;

/**
 *
 * @author Alexey Vladykin
 */
public class InitCommand implements Command{

    public void execute(String[] args) throws PbException {
        DiskRepository repository = DiskRepository.create(new File("."));
    }

}
