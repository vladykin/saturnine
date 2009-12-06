/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.saturnine.disk.impl;

import org.saturnine.api.FileState;

/**
 *
 * @author Alexey Vladykin
 */
public class FileStateImpl implements FileState {

    public static FileState parse(String line) {
        String[] components = line.split("\\s+", 2);
        return new FileStateImpl(components[1], Long.parseLong(components[0]));
    }

    private final String path;
    private final long size;

    /*package*/ FileStateImpl(String path, long size) {
        this.path = path;
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getTimeModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getHash() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileStateImpl)) {
            return false;
        }

        final FileStateImpl that = (FileStateImpl) obj;
        return this.path.equals(that.path) && this.size == that.size;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
