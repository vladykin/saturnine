package org.saturnine.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
import org.saturnine.lib.IOUtil;
import org.saturnine.util.RecordSet;

/**
 * @author Alexey Vladykin
 */
public final class Dirlog {

    public static Dirlog create(File file) throws IOException {
        return new Dirlog(file, true);
    }

    public static Dirlog open(File file) throws IOException {
        return new Dirlog(file, false);
    }

    private final RecordSet recordset;

    private Dirlog(File file, boolean create) throws IOException {
        this.recordset = create ? RecordSet.create(file) : RecordSet.open(file);
    }

    public DirDiff diff(String id) {
        return null;
    }

    public DirDiff diff(String id1, String id2) {
        return null;
    }

    public Map<String, FileInfo> state(String id) {
        return null;
    }

    public Reader newReader() throws IOException {
        return new Reader();
    }

    public Builder newBuilder() throws IOException {
        // TODO: locking
        return new Builder();
    }

    public final class Reader {

        private final RecordSet.Reader delegate;

        private Reader() throws IOException {
            delegate = recordset.newReader();
        }

        public DirDiff next() throws IOException {
            if (!delegate.next()) {
                return null;
            }

            DataInputStream inputStream = new DataInputStream(delegate.inputStream());
            try {
                return IOUtil.readDirDiff(inputStream);
            } finally {
                inputStream.close();
            }
        }

        public void close() throws IOException {
            delegate.close();
        }
    }

    public final class Builder {

        private final RecordSet.Writer delegate;
        private String newState;
        private String oldState;
        private Map<String, FileInfo> addedFiles;
        private Map<String, FileInfo> modifiedFiles;
        private Set<String> removedFiles;
        private Map<String, String> origins;

        private Builder() throws IOException {
            this.delegate = recordset.newWriter();
            setDefaults();
        }
        
        public Builder newState(String newState) {
            this.newState = newState;
            return this;
        }

        public Builder oldState(String oldState) {
            this.oldState = oldState;
            return this;
        }

        public Builder addedFiles(Map<String, FileInfo> addedFiles) {
            this.addedFiles = addedFiles;
            return this;
        }

        public Builder modifiedFiles(Map<String, FileInfo> modifiedFiles) {
            this.modifiedFiles = modifiedFiles;
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

        public DirDiff closeDiff() throws IOException {
            // TODO: check data
            DirDiff diff = new DirDiff(newState, oldState, addedFiles, modifiedFiles, removedFiles, origins);
            DataOutputStream outputStream = new DataOutputStream(delegate.outputStream());
            try {
                IOUtil.writeDirDiff(outputStream, diff);
                outputStream.close();
            } finally {
                delegate.closeRecord();
            }
            setDefaults();
            return diff;
        }

        public void close() throws IOException {
            delegate.close();
        }

        private void setDefaults() {
            newState = null;
            oldState = null;
            addedFiles = Collections.emptyMap();
            modifiedFiles = Collections.emptyMap();
            removedFiles = Collections.emptySet();
            origins = Collections.emptyMap();
        }
    }
}
