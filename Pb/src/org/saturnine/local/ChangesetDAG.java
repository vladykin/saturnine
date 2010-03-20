package org.saturnine.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.saturnine.api.ChangesetInfo;

/**
 *
 * @author Alexey Vladykin
 */
/*package*/ final class ChangesetDAG {

    @SuppressWarnings("unchecked")
    public static ChangesetDAG read(File file) throws IOException {
        Map<String, ChangesetInfo> changesets;

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        try {
                changesets = (Map<String, ChangesetInfo>) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } finally {
            ois.close();
        }

        return new ChangesetDAG(file, changesets);
    }

    public static ChangesetDAG create(File file) {
        return new ChangesetDAG(file, new HashMap<String, ChangesetInfo>());
    }

    private final File file;
    private final Map<String, ChangesetInfo> changesets;

    private ChangesetDAG(File file, Map<String, ChangesetInfo> changesets) {
        this.file = file;
        this.changesets = changesets;
    }

    public void addChangeset(ChangesetInfo changeset) {
        if (!changeset.id().equals(ChangesetInfo.NULL_ID) &&
                (!changesets.containsKey(changeset.primaryParent()) ||
                !changesets.containsKey(changeset.secondaryParent()))) {
            throw new IllegalArgumentException();
        }
        changesets.put(changeset.id(), changeset);
    }

    public ChangesetInfo getChangeset(String id) {
        return changesets.get(id);
    }

    public Collection<ChangesetInfo> getHeads() {
        Map<String, ChangesetInfo> heads = new HashMap<String, ChangesetInfo>(changesets);
        for (ChangesetInfo changeset : changesets.values()) {
            heads.remove(changeset.primaryParent());
            heads.remove(changeset.secondaryParent());
        }
        return heads.values();
    }

    public void write() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        try {
            oos.writeObject(changesets);
        } finally {
            oos.close();
        }
    }
}
