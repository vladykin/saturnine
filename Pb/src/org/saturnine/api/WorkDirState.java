package org.saturnine.api;

import java.util.Set;

/**
 *
 * @author Alexey Vladykin
 */
public interface WorkDirState {

    boolean isClean();

    Set<String> getAddedFiles();

    Set<String> getCleanFiles();

    String getCopyOf(String dest);

    Set<String> getMissingFiles();

    Set<String> getModifiedFiles();

    Set<String> getRemovedFiles();

    Set<String> getUncertainFiles();

    Set<String> getUntrackedFiles();
}
