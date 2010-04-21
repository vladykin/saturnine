package org.saturnine.local;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.saturnine.api.Changeset;
import org.saturnine.util.Hash;

/**
 * @author Alexey Vladykin
 */
public final class Changelog {

    private final RecordSet recordset;

    /*package*/ Changelog(File file) throws IOException {
        this.recordset = new RecordSet(file);
    }

    /**
     * @return collection of changelog heads, i.e. changesets that have no children
     * @throws IOException if an error occurs
     */
    public Collection<Changeset> getHeads() throws IOException {
        Changelog.Reader reader = newChangesetReader();
        try {
            Map<String, Changeset> heads = new HashMap<String, Changeset>();
            for (;;) {
                Changeset changeset = reader.next();
                if (changeset == null) {
                    break;
                }
                heads.put(changeset.id(), changeset);
                heads.remove(changeset.primaryParent());
                heads.remove(changeset.secondaryParent());
            }
            return heads.values();
        } finally {
            reader.close();
        }
    }

    /**
     * Searches changeset by id. This method is preferred to iterating
     * through {@link #allChangesets()} and comparing ids.
     *
     * @param changesetId  id of desired changeset
     * @return changeset with given id, or <code>null</code>
     * @throws NullPointerException if changesetId is <code>null</code>
     * @throws IOException if an error occurs
     */
    public Changeset findChangeset(String changesetId) throws IOException {
        if (changesetId == null) {
            throw new NullPointerException("changesetId is null");
        }
        // TODO: optimize
        Changelog.Reader reader = newChangesetReader();
        try {
            for (;;) {
                Changeset changeset = reader.next();
                if (changeset == null) {
                    break;
                }
                if (changeset.id().equals(changesetId)) {
                    return changeset;
                }
            }
            return null;
        } finally {
            reader.close();
        }
    }

    /**
     * Searches changeset by id. This method is preferred to calling
     * {@link #findChangeset(java.lang.String)} and comparing result to
     * <code>null</code>.
     *
     * @param changesetId  changeset id
     * @return <code>true</code> if changeset exists, <code>false</code> otherwise
     * @throws IOException if an error occurs
     */
    public boolean hasChangeset(String changesetId) throws IOException {
        // TODO: optimize
        return findChangeset(changesetId) != null;
    }

    /**
     * @return iterator that traverses all changesets in topologically sorted
     *      order. Remember to close the iterator once finished.
     * @throws IOException if an error occurs
     */
    public Changelog.Reader newChangesetReader() throws IOException {
        return new Changelog.Reader();
    }

    public Builder newChangesetBuilder() throws IOException {
        return new Changelog.Builder();
    }

    private static Changeset readChangeset(ObjectInputStream inputStream) throws IOException {
        String id = inputStream.readUTF();
        String primaryParent = inputStream.readUTF();
        String secondaryParent = inputStream.readUTF();
        if (secondaryParent.isEmpty()) {
            secondaryParent = null;
        }
        String author = inputStream.readUTF();
        String comment = inputStream.readUTF();
        Date timestamp = new Date(inputStream.readLong());
        return new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp);
    }

    private static void writeChangeset(ObjectOutputStream outputStream, Changeset changeset) throws IOException {
        outputStream.writeUTF(changeset.id());
        outputStream.writeUTF(changeset.primaryParent());
        if (changeset.secondaryParent() != null) {
            outputStream.writeUTF(changeset.secondaryParent());
        } else {
            outputStream.writeUTF("");
        }
        outputStream.writeUTF(changeset.author());
        outputStream.writeUTF(changeset.comment());
        outputStream.writeLong(changeset.timestamp().getTime());
    }

    public final class Reader {

        private final RecordSet.Reader delegate;

        private Reader() throws IOException {
            delegate = recordset.newReader();
        }

        public Changeset next() throws IOException {
            if (!delegate.next()) {
                return null;
            }

            ObjectInputStream inputStream = new ObjectInputStream(delegate.inputStream());
            try {
                return readChangeset(inputStream);
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
        private String id;
        private String primaryParent;
        private String secondaryParent;
        private String author;
        private String comment;
        private Date timestamp;

        private Builder() throws IOException {
            delegate = recordset.newWriter();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder primaryParent(String secondaryParent) {
            this.primaryParent = secondaryParent;
            return this;
        }

        public Builder secondaryParent(String secondaryParent) {
            this.secondaryParent = secondaryParent;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder timestamp(Date timestamp) {
            this.timestamp = (Date) timestamp.clone();
            return this;
        }

        public Changeset closeChangeset() throws IOException {
            checkData();

            Changeset changeset = new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp);
            delegate.next();
            ObjectOutputStream outputStream = new ObjectOutputStream(delegate.outputStream());
            try {
                writeChangeset(outputStream, changeset);
            } finally {
                outputStream.close();
            }

            id = null;
            primaryParent = null;
            secondaryParent = null;
            author = null;
            comment = null;
            timestamp = null;

            return changeset;
        }

        public void close() throws IOException {
            delegate.close();
        }

        private void checkData() {
            if (primaryParent == null) {
                throw new IllegalArgumentException("primaryParent is null");
            }
            if (author == null) {
                throw new IllegalArgumentException("author is null");
            }
            if (comment == null) {
                throw new IllegalArgumentException("comment is null");
            }
            if (timestamp == null) {
                timestamp = new Date();
            }
            if (id == null) {
                id = generateId();
            }
        }

        private String generateId() {
            // TODO: verify that id does not appear in changelog yet
            Hash h = Hash.createSHA1();
            h.update(primaryParent);
            if (secondaryParent != null) {
                h.update(secondaryParent);
            }
            h.update(author);
            h.update(comment);
            h.update(timestamp.toString());
            return h.resultAsHex();
        }
    }
}
