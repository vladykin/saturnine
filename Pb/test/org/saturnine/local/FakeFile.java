package org.saturnine.local;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexey Vladykin
 */
public class FakeFile extends File {

    private final long size;
    private final long lastModified;
    private final List<FakeFile> children;

    public FakeFile(String path) {
        this(path, -1, -1);
    }

    public FakeFile(String path, long size, long lastModified) {
        super(path);
        this.size = size;
        this.lastModified = lastModified;
        this.children = new ArrayList<FakeFile>();
    }

    public FakeFile addChildDir(String name) {
        return addChildFile(name, -1, -1);
    }

    public FakeFile addChildFile(String name, long size, long lastModified) {
        FakeFile newChild = new FakeFile(getPath() + "/" + name, size, lastModified);
        children.add(newChild);
        return newChild;
    }

    @Override
    public boolean isDirectory() {
        return size == -1;
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public long length() {
        return size;
    }

    @Override
    public File[] listFiles(FilenameFilter filter) {
        return children.toArray(new File[children.size()]);
    }
}
