package org.saturnine.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
import org.saturnine.util.Hash;
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
    private final Map<String, String> index;

    private Dirlog(File file, boolean create) throws IOException {
        this.recordset = create ? RecordSet.create(file) : RecordSet.open(file);
        this.index = new HashMap<String, String>();
        if (!create) {
            rebuildIndex();
        }
    }

//    public DirDiff diff(String id) {
//        return null;
//    }
//
//    public DirDiff diff(String id1, String id2) {
//        return null;
//    }

    public Map<String, FileInfo> state(String id) throws IOException {
        List<String> ids = new ArrayList<String>();
        while (!id.equals(Changeset.NULL)) {
            ids.add(id);
            id = index.get(id);
        }
        Collections.reverse(ids);

        Dirlog.Reader reader = new Reader();
        try {
            Map<String, FileInfo> state = new HashMap<String, FileInfo>();
            int expectedIdPos = 0;
            while (expectedIdPos < ids.size()) {
                DirDiff diff = reader.next();
                if (diff == null) {
                    throw new IOException("Reached eof while looking for " + ids.get(expectedIdPos));
                }
                if (diff.newState().equals(ids.get(expectedIdPos))) {
                    ++expectedIdPos;
                    applyDiff(state, diff);
                }
            }
            return state;
        } finally {
            reader.close();
        }
    }

    public Reader newReader() throws IOException {
        return new Reader();
    }

    public Builder newBuilder() throws IOException {
        // TODO: locking
        return new Builder();
    }

    private static void applyDiff(Map<String, FileInfo> state, DirDiff diff) {
        for (FileInfo fileInfo : diff.addedFiles().values()) {
            state.put(fileInfo.path(), fileInfo);
        }
        for (FileInfo fileInfo : diff.modifiedFiles().values()) {
            state.put(fileInfo.path(), fileInfo);
        }
        for (String path : diff.removedFiles()) {
            state.remove(path);
        }
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
                return DataIO.readDirDiff(inputStream);
            } finally {
                inputStream.close();
            }
        }

        public void close() throws IOException {
            delegate.close();
        }
    }

    private void rebuildIndex() throws IOException {
        Dirlog.Reader reader = newReader();
        try {
            index.clear();
            for (;;) {
                DirDiff diff = reader.next();
                if (diff == null) {
                    break;
                }
                index.put(diff.newState(), diff.oldState());
            }
        } finally {
            reader.close();
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

        public Builder addedFile(FileInfo addedFile) {
            addedFiles.put(addedFile.path(), addedFile);
            removedFiles.remove(addedFile.path());
            return this;
        }

        public Builder modifiedFile(FileInfo modifiedFile) {
            modifiedFiles.put(modifiedFile.path(), modifiedFile);
            return this;
        }

        public Builder removedFile(String removedFile) {
            removedFiles.add(removedFile);
            addedFiles.remove(removedFile);
            return this;
        }

        public Builder origin(String dst, String src) {
            origins.put(dst, src);
            return this;
        }

        public DirDiff writeDiff() throws IOException {
            checkData();
            DirDiff diff = new DirDiff(newState, oldState, addedFiles, modifiedFiles, removedFiles, origins);
            DataOutputStream outputStream = new DataOutputStream(delegate.outputStream());
            try {
                DataIO.writeDirDiff(outputStream, diff);
                outputStream.close();
            } finally {
                delegate.writeRecord();
            }
            index.put(newState, oldState);
            setDefaults();
            return diff;
        }

        public void close() throws IOException {
            delegate.close();
        }

        private void setDefaults() {
            newState = null;
            oldState = null;
            addedFiles = new HashMap<String, FileInfo>();
            modifiedFiles = new HashMap<String, FileInfo>();
            removedFiles = new HashSet<String>();
            origins = new HashMap<String, String>();
        }

        private void checkData() {
            if (newState == null) {
                newState = generateId();
            }
            if (oldState == null) {
                oldState = Changeset.NULL;
            }
        }

        private String generateId() {
            // TODO: verify that id does not appear in changelog yet
            Hash h = Hash.createSHA1();
            h.update(oldState);
            h.update(new Date().getTime());
            for (String addedFile : addedFiles.keySet()) {
                h.update(addedFile);
            }
            for (String modifiedFile : modifiedFiles.keySet()) {
                h.update(modifiedFile);
            }
            for (String removedFile : removedFiles) {
                h.update(removedFile);
            }
            return h.resultAsHex();
        }
    }
}
