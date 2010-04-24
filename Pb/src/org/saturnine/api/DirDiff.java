package org.saturnine.api;

import java.util.Map;
import java.util.Set;

/**
 * @author Alexey Vladykin
 */
public final class DirDiff {

    private final String newState;
    private final String oldState;
    private final Map<String, FileInfo> addedFiles;
    private final Map<String, FileInfo> modifiedFiles;
    private final Set<String> removedFiles;
    private final Map<String, String> origins;

    /**
     * Only trusted clients should use this constructor. Thus no copying
     * is perfomed.
     *
     * TODO: what about wrapping in Collections.unmodifiableXXX()?
     *
     * @param oldState
     * @param newState
     * @param addedFiles
     * @param modifiedFiles
     * @param removedFiles
     * @param origins
     */
    public DirDiff(String newState, String oldState, Map<String, FileInfo> addedFiles, Map<String, FileInfo> modifiedFiles, Set<String> removedFiles, Map<String, String> origins) {
        this.newState = newState;
        this.oldState = oldState;
        this.addedFiles = addedFiles;
        this.modifiedFiles = modifiedFiles;
        this.removedFiles = removedFiles;
        this.origins = origins;
    }

    public String newState() {
        return newState;
    }

    public String oldState() {
        return oldState;
    }

    public Map<String, FileInfo> addedFiles() {
        return addedFiles;
    }

    public Map<String, FileInfo> modifiedFiles() {
        return modifiedFiles;
    }

    public Set<String> removedFiles() {
        return removedFiles;
    }

    public Map<String, String> origins() {
        return origins;
    }
}
