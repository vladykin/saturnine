package org.saturnine.disk.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.saturnine.api.Changeset;
import org.saturnine.api.WorkDir;
import org.saturnine.api.PbException;
import org.saturnine.api.FileChange;
import org.saturnine.util.Hash;

/**
 *
 * @author Alexey Vladykin
 */
/*package*/ class DiskChangeset implements Changeset {

    private final DiskRepository repository;
    private final String id;
    private final String primaryParent;
    private final String secondaryParent;
    private final String author;
    private final String comment;
    private final Date timestamp;

    public DiskChangeset(DiskRepository repository) {
        this.repository = repository;
        this.id = Changeset.NULL_ID;
        this.primaryParent = Changeset.NULL_ID;
        this.secondaryParent = Changeset.NULL_ID;
        this.author = "";
        this.comment = "";
        this.timestamp = new Date(0);
    }

    public DiskChangeset(DiskRepository repository, String id, String primaryParent, String secondaryParent, String author, String comment, Date timestamp) throws PbException {
        this.repository = repository;
        this.id = id;
        this.primaryParent = primaryParent;
        this.secondaryParent = secondaryParent;
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    @Override
    public String getID() {
        return id;
    }

    public String getPrimaryParentID() {
        return primaryParent;
    }

    public String getSecondaryParentID() {
        return secondaryParent;
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

    public static DiskChangeset create(WorkDir dirstate,
            String author,
            String comment,
            List<? extends FileChange> changes) throws PbException {

        DiskRepository repository = (DiskRepository) dirstate.getRepository();
        String parentID = dirstate.getParentChangesetID();
        String changesetID = createID(repository, parentID, changes);
        DiskChangeset changeset = new DiskChangeset(
                repository, changesetID, parentID, Changeset.NULL_ID,
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
