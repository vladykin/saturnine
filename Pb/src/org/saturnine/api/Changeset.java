package org.saturnine.api;

import java.util.Set;

/**
 * @author Alexey Vladykin
 */
public interface Changeset {

    Set<FileInfo> addedFiles();

    Set<FileInfo> modifiedFiles();

    Set<String> removedFiles();
}
