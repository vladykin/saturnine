package org.saturnine.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
import org.saturnine.local.DirState.FileAttrs;

/**
 * @author Alexey Vladykin
 */
/*package*/ class DataIO {

    private DataIO() {
    }

    public static FileInfo readFileInfo(DataInputStream inputStream) throws IOException {
        String path = inputStream.readUTF();
        long size = inputStream.readLong();
        short mode = inputStream.readShort();
        String checksum = inputStream.readUTF();
        return new FileInfo(path, size, mode, checksum);
    }

    public static void writeFileInfo(DataOutputStream outputStream, FileInfo fileInfo) throws IOException {
        outputStream.writeUTF(fileInfo.path());
        outputStream.writeLong(fileInfo.size());
        outputStream.writeShort(fileInfo.mode());
        outputStream.writeUTF(fileInfo.checksum());
    }

    public static FileAttrs readFileAttrs(DataInputStream inputStream) throws IOException {
        short mode = inputStream.readShort();
        long size = inputStream.readLong();
        long timestamp = inputStream.readLong();
        return new FileAttrs(mode, size, timestamp);
    }

    public static void writeFileAttrs(DataOutputStream outputStream, FileAttrs fileAttrs) throws IOException {
        outputStream.writeShort(fileAttrs.mode());
        outputStream.writeLong(fileAttrs.size());
        outputStream.writeLong(fileAttrs.timestamp());
    }

    public static Map<String, FileAttrs> readFileAttrsMap(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        Map<String, FileAttrs> map = new HashMap<String, FileAttrs>(3 * size / 2);
        for (int i = 0; i < size; ++i) {
            String path = inputStream.readUTF();
            FileAttrs fileAttrs = readFileAttrs(inputStream);
            map.put(path, fileAttrs);
        }
        return map;
    }

    public static void writeFileAttrsMap(DataOutputStream outputStream, Map<String, FileAttrs> map) throws IOException {
        outputStream.writeInt(map.size());
        for (Map.Entry<String, FileAttrs> entry : map.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            writeFileAttrs(outputStream, entry.getValue());
        }
    }

    public static Map<String, FileInfo> readFileInfoMap(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        Map<String, FileInfo> map = new HashMap<String, FileInfo>(3 * size / 2);
        for (int i = 0; i < size; ++i) {
            FileInfo fileInfo = readFileInfo(inputStream);
            map.put(fileInfo.path(), fileInfo);
        }
        return map;
    }

    public static void writeFileInfoMap(DataOutputStream outputStream, Map<String, FileInfo> map) throws IOException {
        outputStream.writeInt(map.size());
        for (FileInfo fileInfo : map.values()) {
            writeFileInfo(outputStream, fileInfo);
        }
    }

    public static Set<String> readStringSet(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        Set<String> set = new HashSet<String>(3 * size / 2);
        for (int i = 0; i < size; ++i) {
            set.add(inputStream.readUTF());
        }
        return set;
    }

    public static void writeStringSet(DataOutputStream outputStream, Set<String> set) throws IOException {
        outputStream.writeInt(set.size());
        for (String item : set) {
            outputStream.writeUTF(item);
        }
    }

    public static Map<String, String> readStringMap(DataInputStream inputStream) throws IOException {
        int size = inputStream.readInt();
        Map<String, String> map = new HashMap<String, String>(3 * size / 2);
        for (int i = 0; i < size; ++i) {
            String key = inputStream.readUTF();
            String value = readStringOrNull(inputStream);
            map.put(key, value);
        }
        return map;
    }

    public static void writeStringMap(DataOutputStream outputStream, Map<String, String> map) throws IOException {
        outputStream.writeInt(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            writeStringOrNull(outputStream, entry.getValue());
        }
    }

    public static DirDiff readDirDiff(DataInputStream inputStream) throws IOException {
        String newState = inputStream.readUTF();
        String oldState = inputStream.readUTF();
        Map<String, FileInfo> addedFiles = readFileInfoMap(inputStream);
        Map<String, FileInfo> modifiedFiles = readFileInfoMap(inputStream);
        Set<String> removedFiles = readStringSet(inputStream);
        Map<String, String> origins = readStringMap(inputStream);
        return new DirDiff(newState, oldState, addedFiles, modifiedFiles, removedFiles, origins);
    }

    public static void writeDirDiff(DataOutputStream outputStream, DirDiff diff) throws IOException {
        outputStream.writeUTF(diff.newState());
        outputStream.writeUTF(diff.oldState());
        writeFileInfoMap(outputStream, diff.addedFiles());
        writeFileInfoMap(outputStream, diff.modifiedFiles());
        writeStringSet(outputStream, diff.removedFiles());
        writeStringMap(outputStream, diff.origins());
    }

    public static Changeset readChangeset(DataInputStream inputStream) throws IOException {
        String id = inputStream.readUTF();
        String primaryParent = inputStream.readUTF();
        String secondaryParent = readStringOrNull(inputStream);
        String author = inputStream.readUTF();
        String comment = inputStream.readUTF();
        long timestamp = inputStream.readLong();
        return new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp);
    }

    public static void writeChangeset(DataOutputStream outputStream, Changeset changeset) throws IOException {
        outputStream.writeUTF(changeset.id());
        outputStream.writeUTF(changeset.primaryParent());
        writeStringOrNull(outputStream, changeset.secondaryParent());
        outputStream.writeUTF(changeset.author());
        outputStream.writeUTF(changeset.comment());
        outputStream.writeLong(changeset.timestamp());
    }

    private static String readStringOrNull(DataInputStream inputStream) throws IOException {
        if (inputStream.readBoolean()) {
            return inputStream.readUTF();
        } else {
            return null;
        }
    }

    private static void writeStringOrNull(DataOutputStream outputStream, String str) throws IOException {
        if (str == null) {
            outputStream.writeBoolean(false);
        } else {
            outputStream.writeBoolean(true);
            outputStream.writeUTF(str);
        }
    }
}
