package org.saturnine.lib;

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

/**
 * @author Alexey Vladykin
 */
public class IOUtil {

    private IOUtil() {
    }

    public static FileInfo readFileInfo(DataInputStream inputStream) throws IOException {
        String path = inputStream.readUTF();
        long size = inputStream.readLong();
        short mode = inputStream.readShort();
        long lastModified = inputStream.readLong();
        String checksum = inputStream.readUTF();
        return new FileInfo(path, size, mode, lastModified, checksum);
    }

    public static void writeFileInfo(DataOutputStream outputStream, FileInfo fileInfo) throws IOException {
        outputStream.writeUTF(fileInfo.path());
        outputStream.writeLong(fileInfo.size());
        outputStream.writeShort(fileInfo.mode());
        outputStream.writeLong(fileInfo.lastModified());
        outputStream.writeUTF(fileInfo.checksum());
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
            map.put(inputStream.readUTF(), inputStream.readUTF());
        }
        return map;
    }

    public static void writeStringMap(DataOutputStream outputStream, Map<String, String> map) throws IOException {
        outputStream.writeInt(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            outputStream.writeUTF(entry.getKey());
            outputStream.writeUTF(entry.getValue());
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
        String secondaryParent = inputStream.readUTF();
        if (secondaryParent.isEmpty()) {
            secondaryParent = null;
        }
        String author = inputStream.readUTF();
        String comment = inputStream.readUTF();
        long timestamp = inputStream.readLong();
        return new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp);
    }

    public static void writeChangeset(DataOutputStream outputStream, Changeset changeset) throws IOException {
        outputStream.writeUTF(changeset.id());
        outputStream.writeUTF(changeset.primaryParent());
        if (changeset.secondaryParent() != null) {
            outputStream.writeUTF(changeset.secondaryParent());
        } else {
            outputStream.writeUTF("");
        }
        outputStream.writeUTF(changeset.author());
        outputStream.writeUTF(changeset.comment());
        outputStream.writeLong(changeset.timestamp());
    }
}
