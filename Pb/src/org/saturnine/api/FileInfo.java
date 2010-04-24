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

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FileInfo)) {
            return false;
        }
        final FileInfo that = (FileInfo) obj;
        return this.path.equals(that.path) &&
                this.size == that.size &&
                this.mode == that.mode &&
                this.lastModified == that.lastModified &&
                this.checksum.equals(that.checksum);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.path.hashCode();
        hash = 37 * hash + (int) (this.size ^ (this.size >>> 32));
        hash = 37 * hash + this.mode;
        hash = 37 * hash + (int) (this.lastModified ^ (this.lastModified >>> 32));
        hash = 37 * hash + this.checksum.hashCode();
        return hash;
    }
}
