package org.saturnine.api;

/**
 * @author Alexey Vladykin
 */
public final class Changeset {

    public static final String NULL =
            "0000000000000000000000000000000000000000";

    private final String id;
    private final String primaryParent;
    private final String secondaryParent;
    private final String author;
    private final String comment;
    private final long timestamp;

    public Changeset(
            String id, String primaryParent, String secondaryParent,
            String author, String comment, long timestamp) {
        this.id = id;
        this.primaryParent = primaryParent;
        this.secondaryParent = secondaryParent;
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String id() {
        return id;
    }

    public String primaryParent() {
        return primaryParent;
    }

    public String secondaryParent() {
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
        return id;
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
