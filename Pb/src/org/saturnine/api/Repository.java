package org.saturnine.api;

import java.util.Collection;

/**
 * @author Alexey Vladykin
 */
public interface Repository {

    String getURL();

    String getProperty(String key);

    WorkDir getWorkDir() throws PbException;

    Collection<Changeset> getHeads() throws PbException;

    Changeset getChangeset(String id) throws PbException;

    void pull(Repository parent) throws PbException;
}
