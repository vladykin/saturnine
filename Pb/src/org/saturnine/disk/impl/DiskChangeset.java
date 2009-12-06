/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saturnine.disk.impl;

import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import org.saturnine.api.Changeset;
import org.saturnine.api.FileChange;
import org.saturnine.api.PbException;
import org.saturnine.api.UncommittedFileChange;
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
    private final long timestamp;

    public DiskChangeset(DiskRepository repository) {
        this.repository = repository;
        this.id = Changeset.NULL_ID;
        this.parents = Collections.emptyList();
        this.author = "";
        this.comment = "";
        this.timestamp = 0;
    }

    public DiskChangeset(DiskRepository repository, String id, List<String> parents, String author, String comment, long timestamp) throws PbException {
        this.repository = repository;
        this.id = id;
        this.parents = Collections.unmodifiableList(Utils.packedListCopy(parents));
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getID() {
        return id;
    }

    public List<String> getParentIDs() {
        return parents;
    }

    public String getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<FileChange> getFileChanges() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static DiskChangeset create(DiskRepository repository,
            String author,
            String comment,
            List<? extends UncommittedFileChange> changes) throws PbException {

        String parentID = repository.getCurrentID();
        String changesetID = createID(repository, parentID, changes);
        DiskChangeset changeset = new DiskChangeset(
                repository, changesetID, Collections.singletonList(parentID),
                author, comment, System.currentTimeMillis());

        try {
            FileWriter writer = new FileWriter(repository.metadataFile(DiskRepository.CHANGESETS + "/" + changesetID));
            writer.write(parentID + "\n");
            writer.write(changeset.getAuthor() + "\n");
            writer.write(changeset.getComment() + "\n");
            writer.write(changeset.getTimestamp() + "\n");
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }

        return changeset;
    }

    private static String createID(DiskRepository repository, String parentID, List<? extends UncommittedFileChange> changes) throws PbException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] buf;

            buf = repository.getPath().getBytes("UTF-8");
            md.update(buf, 0, buf.length);

            buf = parentID.getBytes("UTF-8");
            md.update(buf, 0, buf.length);

            for (UncommittedFileChange change : changes) {
                if (change.getOriginalState() != null) {
                    buf = change.getOriginalState().getPath().getBytes("UTF-8");
                    md.update(buf, 0, buf.length);
                }
                if (change.getResultState() != null) {
                    buf = change.getResultState().getPath().getBytes("UTF-8");
                    md.update(buf, 0, buf.length);
                }
            }

            return Utils.convertToHex(md.digest());
        } catch (Exception ex) {
            throw new PbException("Exception", ex);
        }

    }
}
