package org.saturnine.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
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
        return null;
    }

    public Builder newWriter() throws IOException {
        return null;
    }

    private static FileInfo readFileInfo(DataInputStream inputStream) throws IOException {
        String path = inputStream.readUTF();
        long size = inputStream.readLong();
        short mode = inputStream.readShort();
        long lastModified = inputStream.readLong();
        String checksum = inputStream.readUTF();
        return new FileInfo(path, size, mode, lastModified, checksum);
    }

    private static Map<String, FileInfo> readFileInfoMap(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        if (0 < size) {
            Map<String, FileInfo> map = new HashMap<String, FileInfo>();
            for (int i = 0; i < size; ++i) {
                FileInfo fileInfo = readFileInfo(inputStream);
                map.put(fileInfo.path(), fileInfo);
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    private static Set<String> readStringSet(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        if (0 < size) {
            Set<String> set = new LinkedHashSet<String>();
            for (int i = 0; i < size; ++i) {
                set.add(inputStream.readUTF());
            }
            return set;
        } else {
            return Collections.emptySet();
        }
    }

    private static Map<String, String> readStringMap(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        if (0 < size) {
            Map<String, String> map = new LinkedHashMap<String, String>();
            for (int i = 0; i < size; ++i) {
                map.put(inputStream.readUTF(), inputStream.readUTF());
            }
            return map;
        } else {
            return Collections.emptyMap();
        }
    }

    private static DirDiff readDiff(DataInputStream inputStream) throws IOException {
        String newState = inputStream.readUTF();
        String oldState = inputStream.readUTF();
        Map<String, FileInfo> addedFiles = readFileInfoMap(inputStream);
        Map<String, FileInfo> modifiedFiles = readFileInfoMap(inputStream);
        Set<String> removedFiles = readStringSet(inputStream);
        Map<String, String> origins = readStringMap(inputStream);
        return new DirDiff(oldState, newState, addedFiles, modifiedFiles, removedFiles, origins);
    }

    private static void writeFileInfo(DataOutputStream outputStream, FileInfo fileInfo) throws IOException {
        outputStream.writeUTF(fileInfo.path());
        outputStream.writeLong(fileInfo.size());
        outputStream.writeShort(fileInfo.mode());
        outputStream.writeLong(fileInfo.lastModified());
        outputStream.writeUTF(fileInfo.checksum());
    }

    private static void writeFileInfoMap(DataOutputStream outputStream, Map<String, FileInfo> map) throws IOException {
        outputStream.writeInt(map.size());
        for (FileInfo fileInfo : map.values()) {
            writeFileInfo(outputStream, fileInfo);
        }
    }

    private static void writeStringSet(DataOutputStream outputStream, Set<String> set) throws IOException {
        outputStream.writeInt(set.size());
        for (String item : set) {
            outputStream.writeUTF(item);
        }
    }

    private static void writeStringMap(DataOutputStream outputStream, Map<String, String> map) throws IOException {
        outputStream.writeInt(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            outputStream.writeUTF(entry.getValue());
        }
    }

    private static void writeDiff(DataOutputStream outputStream, DirDiff diff) throws IOException {
        outputStream.writeUTF(diff.newState());
        outputStream.writeUTF(diff.oldState());
        writeFileInfoMap(outputStream, diff.addedFiles());
        writeFileInfoMap(outputStream, diff.modifiedFiles());
        writeStringSet(outputStream, diff.removedFiles());
        writeStringMap(outputStream, diff.origins());
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
                return readDiff(inputStream);
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
                writeDiff(outputStream, diff);
            } finally {
                outputStream.close();
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
