package org.saturnine.api;

import java.util.Date;
import java.util.List;

/**
 * @author Alexey Vladykin
 */
public interface Changeset {

    static final String NULL_ID = "0000000000000000000000000000000000000000";

    String getID();

    String getPrimaryParentID();

    String getSecondaryParentID();

    String getAuthor();

    String getComment();

    Date getTimestamp();

    List<FileChange> getFileChanges();
}
