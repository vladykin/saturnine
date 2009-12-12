package org.saturnine.disk.impl;

import org.saturnine.api.FileChangeType;
import org.saturnine.api.FileChange;

/**
 * @author Alexey Vladykin
 */
public class FileChangeImpl implements FileChange {

    private final String path;
    private final FileChangeType type;
    private final String copyOf;

    public FileChangeImpl(String path, FileChangeType type, String copyOf) {
        this.path = path;
        this.type = type;
        this.copyOf = copyOf;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public FileChangeType getType() {
        return type;
    }

    @Override
    public String getCopyOf() {
        return copyOf;
    }
}
