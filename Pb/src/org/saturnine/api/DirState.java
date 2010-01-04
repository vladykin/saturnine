package org.saturnine.api;

import java.util.Collection;

/**
 * @author Alexey Vladykin
 */
public interface DirState {

    Repository getRepository();

    Collection<String> getParentChangesetIDs();

    Collection<FileChange> getWorkDirChanges(Collection<String> paths) throws PbException;

    boolean isAboutToAdd(String path) throws PbException;

    void add(Collection<String> paths) throws PbException;

    boolean isAboutToRemove(String path) throws PbException;

    void remove(Collection<String> paths) throws PbException;

    void move(String oldPath, String newPath) throws PbException;

    void commit(String author, String message, Collection<String> paths) throws PbException;
}
