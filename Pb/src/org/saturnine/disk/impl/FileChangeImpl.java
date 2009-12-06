/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.disk.impl;

import org.saturnine.api.FileChangeType;
import org.saturnine.api.FileState;
import org.saturnine.api.UncommittedFileChange;

/**
 *
 * @author Alexey Vladykin
 */
public class FileChangeImpl implements UncommittedFileChange {

    private final FileChangeType type;
    private final FileState orig;
    private final FileState result;
    private final boolean approved;

    public FileChangeImpl(FileChangeType type, FileState orig, FileState result, boolean approved) {
        this.type = type;
        this.orig = orig;
        this.result = result;
        this.approved = approved;
    }

    public long getTimestamp() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isApproved() {
        return approved;
    }

    public FileChangeType getType() {
        return type;
    }

    public FileState getOriginalState() {
        return orig;
    }

    public FileState getResultState() {
        return result;
    }
}
