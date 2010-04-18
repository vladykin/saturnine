package org.saturnine.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.saturnine.api.Changes;
import org.saturnine.api.Changeset;
import org.saturnine.api.PbException;
import org.saturnine.util.Hash;

/**
 * @author Alexey Vladykin
 */
public final class Changelog {

    private final File file;

    /*package*/ Changelog(File file) {
        this.file = file;
    }

    /**
     * @return collection of changelog heads, i.e. changesets that have no children
     * @throws PbException if an error occurs
     */
    public Collection<Changeset> getHeads() throws PbException {
        Changelog.Iterator iterator = getChangesets();
        try {
            Map<String, Changeset> heads = new HashMap<String, Changeset>();
            while (iterator.hasNext()) {
                Changeset changeset = iterator.next();
                heads.put(changeset.id(), changeset);
                heads.remove(changeset.primaryParent());
                heads.remove(changeset.secondaryParent());
            }
            return heads.values();
        } finally {
            iterator.close();
        }
    }

    /**
     * @return iterator that traverses all changesets in topologically sorted
     *      order. Remember to close the iterator once finished.
     * @throws PbException if an error occurs
     */
    public Changelog.Iterator getChangesets() throws PbException {
        if (!file.exists()) {
            return NULL_ITERATOR;
        }
        // TODO: file locking
        try {
            return new Changelog.IteratorImpl(new FileInputStream(file));
        } catch (IOException ex) {
            throw new PbException("Error opening changelog file " + file, ex);
        }
    }

    /**
     * Searches changeset by id. This method is preferred to iterating
     * through {@link #allChangesets()} and comparing ids.
     *
     * @param changesetId  id of desired changeset
     * @return changeset with given id, or <code>null</code>
     * @throws NullPointerException if changesetId is <code>null</code>
     * @throws PbException if an error occurs
     */
    public Changeset findChangeset(String changesetId) throws PbException {
        if (changesetId == null) {
            throw new NullPointerException("changesetId is null");
        }
        // TODO: optimize
        Changelog.Iterator iterator = getChangesets();
        try {
            while (iterator.hasNext()) {
                Changeset changeset = iterator.next();
                if (changeset.id().equals(changesetId)) {
                    return changeset;
                }
            }
            return null;
        } finally {
            iterator.close();
        }
    }

    /**
     * Searches changeset by id. This method is preferred to calling
     * {@link #findChangeset(java.lang.String)} and comparing result to
     * <code>null</code>.
     *
     * @param changesetId  changeset id
     * @return <code>true</code> if changeset exists, <code>false</code> otherwise
     * @throws PbException if an error occurs
     */
    public boolean hasChangeset(String changesetId) throws PbException {
        // TODO: optimize
        return findChangeset(changesetId) != null;
    }

    public Builder newChangesetBuilder() throws PbException {
        return new BuilderImpl();
    }

    /*package*/ static Changeset readChangeset(ObjectInputStream inputStream) throws IOException {
        String id = inputStream.readUTF();
        String primaryParent = inputStream.readUTF();
        String secondaryParent = inputStream.readUTF();
        if (secondaryParent.isEmpty()) {
            secondaryParent = null;
        }
        String author = inputStream.readUTF();
        String comment = inputStream.readUTF();
        Date timestamp = new Date(inputStream.readLong());
        // TODO: read Changes
        return new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp, null);
    }

    /*package*/ static void writeChangeset(ObjectOutputStream outputStream, Changeset changeset) throws IOException {
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
        // TODO: write Changes
    }

    public static interface Iterator {
        boolean hasNext() throws PbException;
        Changeset next() throws PbException;
        void close() throws PbException;
    }

    public static interface Builder {
        Builder id(String id);
        Builder primaryParent(String primaryParent);
        Builder secondaryParent(String secondaryParent);
        Builder author(String author);
        Builder comment(String comment);
        Builder timestamp(Date timestamp);
        Builder changes(Changes changes);
        String add() throws PbException;
        void commit() throws PbException;
    }

    // <editor-fold defaultstate="collapsed" desc="Iterator implementations">
    private static final class IteratorImpl implements Changelog.Iterator {

        private final ObjectInputStream inputStream;
        private boolean hasNext;

        private IteratorImpl(InputStream inputStream) throws IOException {
            this.inputStream = new ObjectInputStream(inputStream);
            hasNext = this.inputStream.readBoolean();
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Changeset next() throws PbException {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            try {
                Changeset changeset = readChangeset(inputStream);
                hasNext = inputStream.readBoolean();
                return changeset;
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }

        @Override
        public void close() throws PbException {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }
    }

    private static final class NullIterator implements Changelog.Iterator {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Changeset next() {
            throw new NoSuchElementException();
        }

        @Override
        public void close() {
        }
    }

    private static final Changelog.Iterator NULL_ITERATOR = new NullIterator();
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Builder implementations">
    private final class BuilderImpl implements Builder {

        private final File tmpFile;
        private ObjectOutputStream outputStream;
        private String id;
        private String primaryParent;
        private String secondaryParent;
        private String author;
        private String comment;
        private Date timestamp;
        private Changes changes;
        private int count;
        
        private BuilderImpl() {
            tmpFile = new File(file.getPath() + ".tmp");
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

        public Builder changes(Changes changes) {
            this.changes = changes;
            return this;
        }

        public String add() throws PbException {
            checkData();
            try {
                if (count == 0) {
                    Changelog.Iterator oldChangesets = getChangesets();
                    try {
                        outputStream = new ObjectOutputStream(new FileOutputStream(tmpFile));
                        while (oldChangesets.hasNext()) {
                            outputStream.writeBoolean(true);
                            outputStream.writeObject(oldChangesets.next());
                        }
                    } finally {
                        oldChangesets.close();
                    }
                }
                outputStream.writeBoolean(true);
                writeChangeset(outputStream, new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp, changes));
                ++count;

                String idBackup = id;
                id = null;
                primaryParent = null;
                secondaryParent = null;
                author = null;
                comment = null;
                timestamp = null;
                changes = null;
                return idBackup;
            } catch (IOException ex) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ex2) {}
                }
                tmpFile.delete();
                throw new PbException("IOException", ex);
            }
        }

        public void commit() throws PbException {
            if (0 < count) {
                try {
                    writeEofAndClose(outputStream);
                } catch (IOException ex) {
                    tmpFile.delete();
                    throw new PbException("IOException", ex);
                }
                if (file.exists() && !file.delete() || !tmpFile.renameTo(file)) {
                    throw new PbException("Failed to move " + tmpFile + " to " + file);
                }
            }
        }

        private void checkData() throws PbException {
            if (primaryParent == null) {
                throw new PbException("primaryParent is null");
            }
            if (author == null) {
                throw new PbException("author is null");
            }
            if (comment == null) {
                throw new PbException("comment is null");
            }
            if (timestamp == null) {
                timestamp = new Date();
            }
            if (id == null) {
                id = generateId();
            }
        }

        private String generateId() {
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

        private void writeEofAndClose(ObjectOutputStream outputStream) throws IOException {
            try {
                outputStream.writeBoolean(false);
            } finally {
                outputStream.close();
            }
        }
    }
    // </editor-fold>
}
