package org.saturnine.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.saturnine.util.HexCharSequence;

/**
 * @author Alexey Vladykin
 */
public final class DirDiff {

    private final HexCharSequence newState;
    private final HexCharSequence oldState;
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
    public DirDiff(CharSequence newState, CharSequence oldState, Map<String, FileInfo> addedFiles, Map<String, FileInfo> modifiedFiles, Set<String> removedFiles, Map<String, String> origins) {
        this.newState = HexCharSequence.get(newState);
        this.oldState = HexCharSequence.get(oldState);
        this.addedFiles = addedFiles;
        this.modifiedFiles = modifiedFiles;
        this.removedFiles = removedFiles;
        this.origins = origins;
    }

    public HexCharSequence newState() {
        return newState;
    }

    public HexCharSequence oldState() {
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

    public DirDiff merge(DirDiff other) {
        if (!newState.equals(other.oldState)) {
            throw new IllegalArgumentException("Attempt to merge incompatible diff");
        }

        Map<String, FileInfo> newAddedFiles = new HashMap<String, FileInfo>(addedFiles());
        Map<String, FileInfo> newModifiedFiles = new HashMap<String, FileInfo>(modifiedFiles());
        Set<String> newRemovedFiles = new HashSet<String>(removedFiles());
        Map<String, String> newOrigins = new HashMap<String, String>(origins());

        for (Map.Entry<String, FileInfo> entry : other.addedFiles().entrySet()) {
            newRemovedFiles.remove(entry.getKey());
            newAddedFiles.put(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, FileInfo> entry : other.modifiedFiles().entrySet()) {
            newModifiedFiles.put(entry.getKey(), entry.getValue());
        }

        for (String removedFile : other.removedFiles()) {
            newAddedFiles.remove(removedFile);
            newOrigins.remove(removedFile);
            newRemovedFiles.add(removedFile);
        }

        for (Map.Entry<String, String> entry : other.origins().entrySet()) {
            String realOrigin = newOrigins.get(entry.getValue());
            newOrigins.put(entry.getKey(), realOrigin == null? entry.getValue() : realOrigin);
        }

        return new DirDiff(other.newState, oldState, newAddedFiles, newModifiedFiles, newRemovedFiles, newOrigins);
    }
}
