package org.saturnine.api;

import java.util.Date;

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
    private final Date timestamp;
    private final Changes changes;

    public Changeset(
            String id, String primaryParent, String secondaryParent,
            String author, String comment, Date timestamp, Changes changes) {
        this.id = id;
        this.primaryParent = primaryParent;
        this.secondaryParent = secondaryParent;
        this.author = author;
        this.comment = comment;
        this.timestamp = timestamp;
        this.changes = changes;
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

    public Date timestamp() {
        return timestamp;
    }

    public Changes changes() {
        return changes;
    }

    @Override
    public String toString() {
        return id;
    }
}
