package org.saturnine.local;

import org.saturnine.api.FileState;

/**
 * @author Alexey Vladykin
 */
/*package*/ class FileStateImpl implements FileState {

    public static FileStateImpl parse(String line) {
        String[] split = line.split("\\s+", 3);
        return new FileStateImpl(split[2], Long.parseLong(split[0]), Long.parseLong(split[1]));
    }

    private final String path;
    private final long size;
    private final long lastModified;

    /*package*/ FileStateImpl(String path, long size, long lastModified) {
        this.path = path;
        this.size = size;
        this.lastModified = lastModified;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getMode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public String getHash() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileStateImpl)) {
            return false;
        }

        final FileStateImpl that = (FileStateImpl) obj;
        return this.size == that.size;
    }

    @Override
    public int hashCode() {
        return (int) (this.size ^ (this.size >> 32));
    }
}
