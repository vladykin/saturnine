/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.api;

import java.util.List;
import org.saturnine.disk.impl.DiskRepository;

/**
 *
 * @author Alexey Vladykin
 */
public interface Repository {

    //URL getURL();

    String getParent() throws PbException;

    String getCurrentID() throws PbException;

    String[] getHeadIDs() throws PbException;

    Changeset getChangeset(String id) throws PbException;

    List<UncommittedFileChange> status() throws PbException;

    void add(String path) throws PbException;

    void remove(String path) throws PbException;

    void move(String path) throws PbException;

    void commit(String author, String message) throws PbException;

    void pull(DiskRepository parent) throws PbException;

    void push(DiskRepository parent) throws PbException;
}
