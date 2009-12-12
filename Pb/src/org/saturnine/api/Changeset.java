package org.saturnine.api;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Alexey Vladykin
 */
public interface Changeset {

    static final String NULL_ID = "0000000000000000000000000000000000000000";

    String getID();

    Collection<String> getParentIDs();

    String getAuthor();

    String getComment();

    Date getTimestamp();

    List<FileChange> getFileChanges();
}
