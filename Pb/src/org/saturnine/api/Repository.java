package org.saturnine.api;

import java.util.Collection;

/**
 * @author Alexey Vladykin
 */
public interface Repository {

    String getURL();

    String getProperty(String key);

    WorkDir getDirState() throws PbException;

    Collection<String> getHeadIDs() throws PbException;

    Changeset getChangeset(String id) throws PbException;

    void pull(Repository parent) throws PbException;
}
