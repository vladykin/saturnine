package org.saturnine.api;

import java.util.Set;

/**
 * @author Alexey Vladykin
 */
public interface Changes {

    Set<FileInfo> addedFiles();

    Set<FileInfo> modifiedFiles();

    Set<String> removedFiles();
}
