package org.saturnine.api;

/**
 * @author Alexey Vladykin
 */
public final class FileInfo {

    private final String path;
    private final long size;
    private final short mode;
    private final long lastModified;
    private final String checksum;

    public FileInfo(
            String path, long size, short mode,
            long lastModified, String checksum) {
        this.path = path;
        this.size = size;
        this.mode = mode;
        this.lastModified = lastModified;
        this.checksum = checksum;
    }

    public String path() {
        return path;
    }

    public long size() {
        return size;
    }

    public int mode() {
        return mode;
    }

    public long lastModified() {
        return lastModified;
    }

    public String checksum() {
        return checksum;
    }

    @Override
    public String toString() {
        return path;
    }
}
