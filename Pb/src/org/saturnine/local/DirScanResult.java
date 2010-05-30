package org.saturnine.local;

import java.util.Map;
import java.util.Set;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public final class DirScanResult {

    private final Map<String, String> addedFiles;
    private final Set<String> removedFiles;
    private final Set<String> cleanFiles;
    private final Set<String> missingFiles;
    private final Set<String> modifiedFiles;
    private final Set<String> uncertainFiles;
    private final Set<String> untrackedFiles;

    public DirScanResult(Map<String, String> addedFiles, Set<String> removedFiles, Set<String> cleanFiles, Set<String> missingFiles, Set<String> modifiedFiles, Set<String> uncertainFiles, Set<String> untrackedFiles) {
        this.addedFiles = addedFiles;
        this.removedFiles = removedFiles;
        this.cleanFiles = cleanFiles;
        this.missingFiles = missingFiles;
        this.modifiedFiles = modifiedFiles;
        this.uncertainFiles = uncertainFiles;
        this.untrackedFiles = untrackedFiles;
    }

    public boolean isClean() {
        return addedFiles.isEmpty() && removedFiles.isEmpty()
                && missingFiles.isEmpty() && modifiedFiles.isEmpty()
                && uncertainFiles.isEmpty();
    }

    public Map<String, String> getAddedFiles() {
        return addedFiles;
    }

    public Set<String> getRemovedFiles() {
        return removedFiles;
    }

    public Set<String> getCleanFiles() {
        return cleanFiles;
    }

    public Set<String> getMissingFiles() {
        return missingFiles;
    }

    public Set<String> getModifiedFiles() {
        return modifiedFiles;
    }

    public Set<String> getUncertainFiles() {
        return uncertainFiles;
    }

    public Set<String> getUntrackedFiles() {
        return untrackedFiles;
    }
}
