package org.saturnine.api;

import org.saturnine.util.HexCharSequence;

/**
 * @author Alexey Vladykin
 */
public final class Changeset {

    public static final HexCharSequence NULL =
            HexCharSequence.get(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});

    private final HexCharSequence id;
    private final HexCharSequence primaryParent;
    private final HexCharSequence secondaryParent;
    private final String author;
    private final String comment;
    private final long timestamp;

    public Changeset(
            CharSequence id, CharSequence primaryParent, CharSequence secondaryParent,
            String author, String comment, long timestamp) {
        this.id = HexCharSequence.get(id);
        this.primaryParent = HexCharSequence.get(primaryParent);
        this.secondaryParent = secondaryParent == null? null : HexCharSequence.get(secondaryParent);
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public HexCharSequence id() {
        return id;
    }

    public HexCharSequence primaryParent() {
        return primaryParent;
    }

    public HexCharSequence secondaryParent() {
        return secondaryParent;
    }

    public String author() {
        return author;
    }

    public String comment() {
        return comment;
    }

    public long timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Changeset)) {
            return false;
        }
        final Changeset that = (Changeset) obj;
        return this.id.equals(that.id)
                && this.primaryParent.equals(that.primaryParent)
                && (this.secondaryParent == null ? that.secondaryParent == null : this.secondaryParent.equals(that.secondaryParent))
                && this.author.equals(that.author)
                && this.comment.equals(that.comment)
                && this.timestamp == that.timestamp;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.id.hashCode();
        hash = 53 * hash + this.primaryParent.hashCode();
        hash = 53 * hash + (this.secondaryParent != null ? this.secondaryParent.hashCode() : 0);
        hash = 53 * hash + this.author.hashCode();
        hash = 53 * hash + this.comment.hashCode();
        hash = 53 * hash + (int) ((this.timestamp >>> 32) ^ this.timestamp);
        return hash;
    }
}
