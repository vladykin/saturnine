/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.api;

import java.util.List;

/**
 *
 * @author Alexey Vladykin
 */
public interface Changeset {

    static final String NULL_ID = "0000000000000000000000000000000000000000";

    String getID();

    List<String> getParentIDs();

    String getAuthor();

    String getComment();

    long getTimestamp();

    List<FileChange> getFileChanges();
}
