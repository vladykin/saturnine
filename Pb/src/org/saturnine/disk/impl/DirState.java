package org.saturnine.disk.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.saturnine.api.FileState;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public final class DirState {

    @SuppressWarnings("unchecked")
    public static DirState read(File dirstate, File basedir) throws IOException {
        Map<String, FileState> knownFileStates;
        Set<String> addedFiles;
        Set<String> removedFiles;
        Map<String, String> copyOf;

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dirstate));
        try {
            knownFileStates = (Map<String, FileState>) ois.readObject();
            addedFiles = (Set<String>) ois.readObject();
            removedFiles = (Set<String>) ois.readObject();
            copyOf = (Map<String, String>) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } finally {
            ois.close();
        }

        return new DirState(dirstate, basedir, knownFileStates, addedFiles, removedFiles, copyOf);
    }

    public static DirState create(File dirstate, File basedir, Map<String, FileState> knownFileStates) {
        return new DirState(dirstate, basedir, knownFileStates,
                new HashSet<String>(), new HashSet<String>(),
                new HashMap<String, String>());
    }

    private final File dirstate;
    private final File basedir;
    private final Map<String, FileState> knownFileStates;
    private final Set<String> addedFiles;
    private final Set<String> removedFiles;
    private final Map<String, String> copyOf;

    private DirState(File dirstate, File basedir,
            Map<String, FileState> knownFileStates,
            Set<String> addedFiles, Set<String> removedFiles,
            Map<String, String> copyOf) {
        this.dirstate = dirstate;
        this.basedir = basedir;
        this.knownFileStates = Utils.immutableMapCopy(knownFileStates);
        this.addedFiles = addedFiles;
        this.removedFiles = removedFiles;
        this.copyOf = copyOf;
    }

    public Set<String> getAddedFiles() {
        return Collections.unmodifiableSet(addedFiles);
    }

    public void setAdded(String file) {
        removedFiles.remove(file);
        if (!knownFileStates.containsKey(file)) {
            addedFiles.add(file);
        }
    }

    public Set<String> getRemovedFiles() {
        return Collections.unmodifiableSet(removedFiles);
    }

    public void setRemoved(String file) {
        addedFiles.remove(file);
        if (knownFileStates.containsKey(file)) {
            removedFiles.add(file);
        }
    }

    public String getCopyOf(String path) {
        return copyOf.get(path);
    }

    public void setCopyOf(String srcFile, String dstFile) {
        if (srcFile == null) {
            copyOf.remove(dstFile);
        } else {
            copyOf.put(dstFile, srcFile);
        }
    }

    public Snapshot createSnapshot() {
        Set<String> clean = new HashSet<String>();
        Set<String> modified = new HashSet<String>();
        Set<String> uncertain = new HashSet<String>();
        Set<String> untracked = new HashSet<String>();
        collectFileStates(basedir, clean, modified, uncertain, untracked);

        Set<String> missing = new HashSet<String>(knownFileStates.keySet());
        missing.removeAll(removedFiles);
        missing.removeAll(clean);
        missing.removeAll(modified);
        missing.removeAll(uncertain);

        return new Snapshot(addedFiles, removedFiles, copyOf, clean, missing, modified, uncertain, untracked);
    }

    public void write() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dirstate));
        try {
            oos.writeObject(knownFileStates);
            oos.writeObject(addedFiles);
            oos.writeObject(removedFiles);
            oos.writeObject(copyOf);
        } finally {
            oos.close();
        }
    }

    public static class Snapshot {

        private final Set<String> addedFiles;
        private final Set<String> removedFiles;
        private final Map<String, String> copyOf;
        private final Set<String> cleanFiles;
        private final Set<String> missingFiles;
        private final Set<String> modifiedFiles;
        private final Set<String> uncertainFiles;
        private final Set<String> untrackedFiles;

        public Snapshot(
                Set<String> addedFiles, Set<String> removedFiles, Map<String, String> copyOf,
                Set<String> cleanFiles, Set<String> missingFiles, Set<String> modifiedFiles,
                Set<String> uncertainFiles, Set<String> untrackedFiles) {
            this.addedFiles = Utils.immutableSetCopy(addedFiles);
            this.removedFiles = Utils.immutableSetCopy(removedFiles);
            this.copyOf = Utils.immutableMapCopy(copyOf);
            this.cleanFiles = Utils.immutableSetCopy(cleanFiles);
            this.missingFiles = Utils.immutableSetCopy(missingFiles);
            this.modifiedFiles = Utils.immutableSetCopy(modifiedFiles);
            this.uncertainFiles = Utils.immutableSetCopy(uncertainFiles);
            this.untrackedFiles = Utils.immutableSetCopy(untrackedFiles);
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

    private void collectFileStates(File dir, Set<String> clean, Set<String> modified, Set<String> uncertain, Set<String> untracked) {
        File[] children = dir.listFiles(new DiskRepository.PbFileFilter());
        for (File f : children) {
            if (f.isDirectory()) {
                collectFileStates(f, clean, modified, uncertain, untracked);
            } else {
                String path = Utils.relativePath(basedir, f);
                FileState knownState = knownFileStates.get(path);
                if (knownState == null) {
                    if (!addedFiles.contains(path)) {
                        untracked.add(path);
                    }
                } else {
                    long newSize = f.length();
                    long newModified = f.lastModified();
                    if (newSize != knownState.getSize()) {
                        modified.add(path);
                    } else if (newModified != knownState.getLastModified()) {
                        uncertain.add(path);
                    } else {
                        clean.add(path);
                    }
                }
            }
        }
    }
}
