package org.saturnine.api;

import java.util.Date;

/**
 * @author Alexey Vladykin
 */
public final class ChangesetInfo {

    public static final String NULL_ID =
            "0000000000000000000000000000000000000000";

    public static final ChangesetInfo NULL =
            new ChangesetInfo(NULL_ID, null, null, null, null, null);

    private final String id;
    private final String primaryParent;
    private final String secondaryParent;
    private final String author;
    private final String comment;
    private final Date timestamp;

    public ChangesetInfo(
            String id, String primaryParent, String secondaryParent,
            String author, String comment, Date timestamp) {
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

    public Date timestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return id;
    }
}
