package org.saturnine.disk.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.saturnine.api.Changeset;

/**
 *
 * @author Alexey Vladykin
 */
public final class ChangesetDAG {

    @SuppressWarnings("unchecked")
    public static ChangesetDAG read(File file) throws IOException {
        Map<String, Changeset> changesets;

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        try {
                changesets = (Map<String, Changeset>) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        } finally {
            ois.close();
        }

        return new ChangesetDAG(file, changesets);
    }

    public static ChangesetDAG create(File file) {
        return new ChangesetDAG(file, new HashMap<String, Changeset>());
    }

    private final File file;
    private final Map<String, Changeset> changesets;

    private ChangesetDAG(File file, Map<String, Changeset> changesets) {
        this.file = file;
        this.changesets = changesets;
    }

    public void addChangeset(Changeset changeset) {
        if (!changeset.getID().equals(Changeset.NULL_ID) &&
                (!changesets.containsKey(changeset.getPrimaryParentID()) ||
                !changesets.containsKey(changeset.getSecondaryParentID()))) {
            throw new IllegalArgumentException();
        }
        changesets.put(changeset.getID(), changeset);
    }

    public Changeset getChangeset(String id) {
        return changesets.get(id);
    }

    public Collection<Changeset> getHeads() {
        Map<String, Changeset> heads = new HashMap<String, Changeset>(changesets);
        for (Changeset changeset : changesets.values()) {
            heads.remove(changeset.getPrimaryParentID());
            heads.remove(changeset.getSecondaryParentID());
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
