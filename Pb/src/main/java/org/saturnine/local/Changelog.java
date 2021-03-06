package org.saturnine.local;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.saturnine.api.Changeset;
import org.saturnine.util.HexCharSequence;
import org.saturnine.util.RecordSet;

/**
 * @author Alexey Vladykin
 */
public final class Changelog {

    public static Changelog create(File file) throws IOException {
        return new Changelog(file, true);
    }

    public static Changelog open(File file) throws IOException {
        return new Changelog(file, false);
    }

    private final RecordSet recordset;

    private Changelog(File file, boolean create) throws IOException {
        this.recordset = create? RecordSet.create(file) : RecordSet.open(file);
    }

    /**
     * @return collection of changelog heads, i.e. changesets that have no children
     * @throws IOException if an error occurs
     */
    public Collection<Changeset> getHeads() throws IOException {
        Changelog.Reader reader = newReader();
        try {
            Map<HexCharSequence, Changeset> heads = new HashMap<HexCharSequence, Changeset>();
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
     * @param id  id of desired changeset
     * @return changeset with given id, or <code>null</code>
     * @throws NullPointerException if changesetId is <code>null</code>
     * @throws IOException if an error occurs
     */
    public Changeset findChangeset(CharSequence id) throws IOException {
        if (id == null) {
            throw new NullPointerException("changesetId is null");
        }
        HexCharSequence hexid = HexCharSequence.get(id);
        // TODO: optimize
        Changelog.Reader reader = newReader();
        try {
            for (;;) {
                Changeset changeset = reader.next();
                if (changeset == null) {
                    break;
                }
                if (changeset.id().equals(hexid)) {
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
     * @param id  changeset id
     * @return <code>true</code> if changeset exists, <code>false</code> otherwise
     * @throws IOException if an error occurs
     */
    public boolean hasChangeset(CharSequence id) throws IOException {
        // TODO: optimize
        return findChangeset(id) != null;
    }

    /**
     * @return iterator that traverses all changesets in topologically sorted
     *      order. Remember to close the iterator once finished.
     * @throws IOException if an error occurs
     */
    public Changelog.Reader newReader() throws IOException {
        return new Changelog.Reader();
    }

    public Builder newBuilder() throws IOException {
        return new Changelog.Builder();
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

            DataInputStream inputStream = new DataInputStream(delegate.inputStream());
            try {
                return DataIO.readChangeset(inputStream);
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
        private HexCharSequence id;
        private HexCharSequence primaryParent;
        private HexCharSequence secondaryParent;
        private String author;
        private String comment;
        private long timestamp;

        private Builder() throws IOException {
            delegate = recordset.newWriter();
        }

        public Builder id(CharSequence id) {
            this.id = HexCharSequence.get(id);
            return this;
        }

        public Builder primaryParent(CharSequence primaryParent) {
            this.primaryParent = HexCharSequence.get(primaryParent);
            return this;
        }

        public Builder secondaryParent(CharSequence secondaryParent) {
            this.secondaryParent = secondaryParent == null ? null : HexCharSequence.get(secondaryParent);
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

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Changeset writeChangeset() throws IOException {
            checkData();

            Changeset changeset = new Changeset(id, primaryParent, secondaryParent, author, comment, timestamp);
            DataOutputStream outputStream = new DataOutputStream(delegate.outputStream());
            try {
                DataIO.writeChangeset(outputStream, changeset);
                outputStream.close();
            } finally {
                delegate.writeRecord();
            }

            id = null;
            primaryParent = null;
            secondaryParent = null;
            author = null;
            comment = null;
            timestamp = 0;

            return changeset;
        }

        public void close() throws IOException {
            delegate.close();
        }

        private void checkData() {
            if (id == null) {
                throw new IllegalArgumentException("id is null");
            }
            if (primaryParent == null) {
                throw new IllegalArgumentException("primaryParent is null");
            }
            if (author == null) {
                throw new IllegalArgumentException("author is null");
            }
            if (comment == null) {
                throw new IllegalArgumentException("comment is null");
            }
            if (timestamp == 0) {
                timestamp = new Date().getTime();
            }
        }
    }
}
