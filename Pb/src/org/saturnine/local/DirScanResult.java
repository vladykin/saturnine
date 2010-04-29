package org.saturnine.local;

import java.util.Map;
import java.util.Set;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public final class DirScanResult {

    private final Set<String> addedFiles;
    private final Set<String> removedFiles;
    private final Map<String, String> copyOf;
    private final Set<String> cleanFiles;
    private final Set<String> missingFiles;
    private final Set<String> modifiedFiles;
    private final Set<String> uncertainFiles;
    private final Set<String> untrackedFiles;

    public DirScanResult(Set<String> addedFiles, Set<String> removedFiles, Map<String, String> copyOf, Set<String> cleanFiles, Set<String> missingFiles, Set<String> modifiedFiles, Set<String> uncertainFiles, Set<String> untrackedFiles) {
        this.addedFiles = Utils.immutableSetCopy(addedFiles);
        this.removedFiles = Utils.immutableSetCopy(removedFiles);
        this.copyOf = Utils.immutableMapCopy(copyOf);
        this.cleanFiles = Utils.immutableSetCopy(cleanFiles);
        this.missingFiles = Utils.immutableSetCopy(missingFiles);
        this.modifiedFiles = Utils.immutableSetCopy(modifiedFiles);
        this.uncertainFiles = Utils.immutableSetCopy(uncertainFiles);
        this.untrackedFiles = Utils.immutableSetCopy(untrackedFiles);
    }

    public boolean isClean() {
        return addedFiles.isEmpty() && removedFiles.isEmpty()
                && missingFiles.isEmpty() && modifiedFiles.isEmpty()
                && uncertainFiles.isEmpty();
    }

    public Set<String> getAddedFiles() {
        return addedFiles;
    }

    public Set<String> getRemovedFiles() {
        return removedFiles;
    }

    public String getCopyOf(String dest) {
        return copyOf.get(dest);
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
