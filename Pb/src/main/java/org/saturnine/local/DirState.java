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
import org.saturnine.util.FileUtil;

/**
 * DirState is {@link WorkDir}'s implementation detail.
 *
 * @author Alexey Vladykin
 */
/*package*/ final class DirState {

    public static DirState open(File dirstate) throws IOException {
        return new DirState(dirstate, false);
    }

    public static DirState create(File dirstate) throws IOException {
        return new DirState(dirstate, true);
    }

    private final File dirstate;

    private DirState(File dirstate, boolean create) throws IOException {
        this.dirstate = dirstate;
        if (create) {
            setState(new State());
        }
    }

    public State getState() throws IOException {
        DataInputStream inputStream = new DataInputStream(new FileInputStream(dirstate));
        try {
            Map<String, FileAttrs> knownFiles = DataIO.readFileAttrsMap(inputStream);
            Map<String, String> addedFiles = DataIO.readStringMap(inputStream);
            Set<String> removedFiles = DataIO.readStringSet(inputStream);
            return new State(knownFiles, addedFiles, removedFiles);
        } finally {
            inputStream.close();
        }
    }

    public void setState(State state) throws IOException {
        File tmpFile = new File(dirstate.getPath() + ".tmp");
        DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(tmpFile));
        try {
            DataIO.writeFileAttrsMap(outputStream, state.knownFiles());
            DataIO.writeStringMap(outputStream, state.addedFiles());
            DataIO.writeStringSet(outputStream, state.removedFiles());
        } finally {
            outputStream.close();
        }
        FileUtil.rename(tmpFile, dirstate);
    }

    public static final class State {
        private final Map<String, FileAttrs> knownFiles;
        private final Map<String, String> addedFiles;
        private final Set<String> removedFiles;

        public State() {
            this(new HashMap<String, FileAttrs>(), new HashMap<String, String>(), new HashSet<String>());
        }

        public State(Map<String, FileAttrs> knownFiles, Map<String, String> addedFiles, Set<String> removedFiles) {
            this.knownFiles = knownFiles;
            this.addedFiles = addedFiles;
            this.removedFiles = removedFiles;
        }

        public Map<String, FileAttrs> knownFiles() {
            return knownFiles;
        }

        public Map<String, String> addedFiles() {
            return addedFiles;
        }

        public Set<String> removedFiles() {
            return removedFiles;
        }
    }

    public static final class FileAttrs {
        private final short mode;
        private final long size;
        private final long timestamp;

        public FileAttrs(short mode, long size, long timestamp) {
            this.mode = mode;
            this.size = size;
            this.timestamp = timestamp;
        }

        public short mode() {
            return mode;
        }

        public long size() {
            return size;
        }

        public long timestamp() {
            return timestamp;
        }
    }
}
