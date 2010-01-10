package org.saturnine.api;

import java.util.Collection;

/**
 * State of repository checkout.
 *
 * @author Alexey Vladykin
 */
public interface WorkDir {

    /**
     * @return id of primary parent changeset
     */
    String getParentChangesetID();

    /**
     * @return id of secondary parent changeset for merges,
     *      or {@link Changeset#NULL_ID} for non-merges
     */
    String getSecondaryChangesetID();

    Repository getRepository();

    WorkDirState scanForChanges(Collection<String> paths) throws PbException;

    void add(Collection<String> paths) throws PbException;

    void remove(Collection<String> paths) throws PbException;

    void copy(String oldPath, String newPath) throws PbException;

    void move(String oldPath, String newPath) throws PbException;

    void commit(String author, String message, Collection<String> paths) throws PbException;
}
