package org.saturnine.disk.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirState;
import org.saturnine.api.PbException;
import org.saturnine.api.FileChange;
import org.saturnine.util.Hash;
import org.saturnine.util.Utils;

/**
 *
 * @author Alexey Vladykin
 */
/*package*/ class DiskChangeset implements Changeset {

    private final DiskRepository repository;
    private final String id;
    private final List<String> parents;
    private final String author;
    private final String comment;
    private final Date timestamp;

    public DiskChangeset(DiskRepository repository) {
        this.repository = repository;
        this.id = Changeset.NULL_ID;
        this.parents = Collections.emptyList();
        this.author = "";
        this.comment = "";
        this.timestamp = new Date(0);
    }

    public DiskChangeset(DiskRepository repository, String id, List<String> parents, String author, String comment, Date timestamp) throws PbException {
        this.repository = repository;
        this.id = id;
        this.parents = Utils.immutableListCopy(parents);
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public List<String> getParentIDs() {
        return parents;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public List<FileChange> getFileChanges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static DiskChangeset create(DirState dirstate,
            String author,
            String comment,
            List<? extends FileChange> changes) throws PbException {

        DiskRepository repository = (DiskRepository) dirstate.getRepository();
        String parentID = dirstate.getParentChangesetID();
        String changesetID = createID(repository, parentID, changes);
        DiskChangeset changeset = new DiskChangeset(
                repository, changesetID, Collections.singletonList(parentID),
                author, comment, new Date());

        try {
            FileWriter writer = new FileWriter(repository.metadataFile(DiskRepository.CHANGESETS + "/" + changesetID));
            writer.write(parentID + "\n");
            writer.write(changeset.getAuthor() + "\n");
            writer.write(changeset.getComment() + "\n");
            writer.write(changeset.getTimestamp().getTime() + "\n");
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }

        return changeset;
    }

    private static String createID(DiskRepository repository, String parentID, List<? extends FileChange> changes) throws PbException {
        try {
            Hash hash = Hash.createSHA1();
            hash.update(repository.getURL());
            hash.update(parentID);

            for (FileChange change : changes) {
                hash.update(change.getPath());
                if (change.getCopyOf() != null) {
                    hash.update(change.getCopyOf());
                }
            }

            return hash.resultAsHex();
        } catch (Exception ex) {
            throw new PbException("Exception", ex);
        }

    }
}
