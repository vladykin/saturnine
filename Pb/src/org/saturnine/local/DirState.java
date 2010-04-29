package org.saturnine.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.saturnine.api.Changeset;
import org.saturnine.api.FileInfo;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public final class DirState {

    public static DirState open(File dirstate, File basedir) throws IOException {
        return new DirState(dirstate, basedir, false);
    }

    public static DirState create(File dirstate, File basedir) throws IOException {
        return new DirState(dirstate, basedir, true);
    }
    private final File dirstate;
    private final File basedir;

    private DirState(File dirstate, File basedir, boolean create) throws IOException {
        this.dirstate = dirstate;
        this.basedir = basedir;
        if (create) {
            newBuilder(false).close();
        }
        if (!dirstate.exists()) {
            throw new IOException(dirstate.getPath() + " does not exist");
        }
    }

    public Snapshot snapshot() throws IOException {
        DataInputStream inputStream = new DataInputStream(new FileInputStream(dirstate));
        try {
            String primaryParent = inputStream.readUTF();
            String secondaryParent = inputStream.readUTF();
            if (secondaryParent.equals("")) {
                secondaryParent = null;
            }
            Map<String, FileInfo> knownFiles = DataIO.readFileInfoMap(inputStream);
            Set<String> addedFiles = DataIO.readStringSet(inputStream);
            Set<String> removedFiles = DataIO.readStringSet(inputStream);
            Map<String, String> origins = DataIO.readStringMap(inputStream);
            return new Snapshot(primaryParent, secondaryParent, knownFiles, addedFiles, removedFiles, origins);
        } finally {
            inputStream.close();
        }
    }

    public Builder newBuilder(boolean initWithCurrentSnapshot) throws IOException {
        // TODO: file locking
        String primaryParent;
        String secondaryParent;
        Map<String, FileInfo> knownFiles;
        Set<String> addedFiles;
        Set<String> removedFiles;
        Map<String, String> origins;
        if (initWithCurrentSnapshot) {
            Snapshot snapshot = snapshot();
            primaryParent = snapshot.primaryParent();
            secondaryParent = snapshot.secondaryParent();
            knownFiles = snapshot.knownFiles();
            addedFiles = snapshot.addedFiles();
            removedFiles = snapshot.removedFiles();
            origins = snapshot.origins();
        } else {
            primaryParent = Changeset.NULL;
            secondaryParent = null;
            knownFiles = new HashMap<String, FileInfo>();
            addedFiles = new HashSet<String>();
            removedFiles = new HashSet<String>();
            origins = new HashMap<String, String>();
        }
        return new Builder(primaryParent, secondaryParent, knownFiles, addedFiles, removedFiles, origins);
    }

    public final class Snapshot {

        private final String primaryParent;
        private final String secondaryParent;
        private final Map<String, FileInfo> knownFiles;
        private final Set<String> addedFiles;
        private final Set<String> removedFiles;
        private final Map<String, String> origins;

        private Snapshot(String primaryParent, String secondaryParent,
                Map<String, FileInfo> knownFiles, Set<String> addedFiles,
                Set<String> removedFiles, Map<String, String> origins) {
            this.primaryParent = primaryParent;
            this.secondaryParent = secondaryParent;
            this.knownFiles = knownFiles;
            this.addedFiles = addedFiles;
            this.removedFiles = removedFiles;
            this.origins = origins;
        }

        public String primaryParent() {
            return primaryParent;
        }

        public String secondaryParent() {
            return secondaryParent;
        }

        public Map<String, FileInfo> knownFiles() {
            return knownFiles;
        }

        public Set<String> addedFiles() {
            return addedFiles;
        }

        public Set<String> removedFiles() {
            return removedFiles;
        }

        public Map<String, String> origins() {
            return origins;
        }

        public DirScanResult scanDir() {
            Set<String> clean = new HashSet<String>();
            Set<String> modified = new HashSet<String>();
            Set<String> uncertain = new HashSet<String>();
            Set<String> untracked = new HashSet<String>();
            collectFileChanges(basedir, clean, modified, uncertain, untracked);

            Set<String> missing = new HashSet<String>(knownFiles.keySet());
            missing.removeAll(removedFiles);
            missing.removeAll(clean);
            missing.removeAll(modified);
            missing.removeAll(uncertain);

            return new DirScanResult(addedFiles, removedFiles, origins, clean, missing, modified, uncertain, untracked);
        }

        private void collectFileChanges(File dir, Set<String> clean, Set<String> modified, Set<String> uncertain, Set<String> untracked) {
            File[] children = dir.listFiles(new LocalRepository.PbFileFilter());
            for (File f : children) {
                if (f.isDirectory()) {
                    collectFileChanges(f, clean, modified, uncertain, untracked);
                } else {
                    String path = Utils.relativePath(basedir, f);
                    FileInfo knownState = knownFiles.get(path);
                    if (knownState == null) {
                        if (!addedFiles.contains(path)) {
                            untracked.add(path);
                        }
                    } else {
                        long newSize = f.length();
                        long newModified = f.lastModified();
                        if (newSize != knownState.size()) {
                            modified.add(path);
                        } else if (newModified != knownState.lastModified()) {
                            uncertain.add(path);
                        } else {
                            clean.add(path);
                        }
                    }
                }
            }
        }
    }

    public final class Builder {

        private String primaryParent;
        private String secondaryParent;
        private Map<String, FileInfo> knownFiles;
        private Set<String> addedFiles;
        private Set<String> removedFiles;
        private Map<String, String> origins;

        private Builder(String primaryParent, String secondaryParent,
                Map<String, FileInfo> knownFiles, Set<String> addedFiles,
                Set<String> removedFiles, Map<String, String> origins) {
            this.primaryParent = primaryParent;
            this.secondaryParent = secondaryParent;
            this.knownFiles = knownFiles;
            this.addedFiles = addedFiles;
            this.removedFiles = removedFiles;
            this.origins = origins;
        }

        public Builder primaryParent(String primaryParent) {
            this.primaryParent = primaryParent;
            return this;
        }

        public Builder secondaryParent(String secondaryParent) {
            this.secondaryParent = secondaryParent;
            return this;
        }

        public Builder knownFiles(Map<String, FileInfo> knownFiles) {
            this.knownFiles = knownFiles;
            return this;
        }

        public Builder addedFiles(Set<String> addedFiles) {
            this.addedFiles = addedFiles;
            return this;
        }

        public Builder removedFiles(Set<String> removedFiles) {
            this.removedFiles = removedFiles;
            return this;
        }

        public Builder origins(Map<String, String> origins) {
            this.origins = origins;
            return this;
        }

        public Snapshot close() throws IOException {
            File tmpFile = new File(dirstate.getPath() + ".tmp");
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(tmpFile));
            try {
                outputStream.writeUTF(primaryParent);
                outputStream.writeUTF(secondaryParent == null? "" : secondaryParent);
                DataIO.writeFileInfoMap(outputStream, knownFiles);
                DataIO.writeStringSet(outputStream, addedFiles);
                DataIO.writeStringSet(outputStream, removedFiles);
                DataIO.writeStringMap(outputStream, origins);
                tmpFile.renameTo(dirstate);
                return new Snapshot(primaryParent, secondaryParent, knownFiles, addedFiles, removedFiles, origins);
            } finally {
                outputStream.close();
            }
        }
    }
}
