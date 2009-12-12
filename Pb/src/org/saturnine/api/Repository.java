package org.saturnine.api;

import java.util.Collection;
import java.util.List;

/**
 * @author Alexey Vladykin
 */
public interface Repository {

    String getPath();

    String getParent() throws PbException;

    String getCurrentID() throws PbException;

    String[] getHeadIDs() throws PbException;

    Changeset getChangeset(String id) throws PbException;

    List<FileChange> getWorkDirChanges(Collection<String> paths) throws PbException;

    boolean isAboutToAdd(String path) throws PbException;

    void add(Collection<String> paths) throws PbException;

    boolean isAboutToRemove(String path) throws PbException;

    void remove(Collection<String> paths) throws PbException;

    void move(String oldPath, String newPath) throws PbException;

    void commit(String author, String message, Collection<String> paths) throws PbException;

    void pull(Repository parent) throws PbException;

    void push(Repository parent) throws PbException;
}
