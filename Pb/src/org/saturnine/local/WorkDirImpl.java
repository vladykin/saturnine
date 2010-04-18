package org.saturnine.local;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.saturnine.api.Changeset;
import org.saturnine.api.WorkDir;
import org.saturnine.api.WorkDirState;
import org.saturnine.api.FileInfo;
import org.saturnine.api.PbException;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
/*package*/ final class WorkDirImpl implements WorkDir {

    private static final String DIRSTATE = "dirstate";

    public static WorkDirImpl create(LocalRepository repository) throws PbException {
        File dirstateFile = repository.metadataFile(DIRSTATE);
        DirState dirstate;
        if (dirstateFile.exists()) {
            try {
                dirstate = DirState.read(dirstateFile, repository.getPath());
            } catch (IOException ex) {
                throw new PbException("Failed to read dirstate", ex);
            }
        } else {
            dirstate = DirState.create(dirstateFile, repository.getPath(),
                    Changeset.NULL, Changeset.NULL,
                    Collections.<String, FileInfo>emptyMap());
        }
        return new WorkDirImpl(repository, dirstate);
    }

    private final LocalRepository repository;
    private DirState dirstate;

    private WorkDirImpl(LocalRepository repository, DirState dirstate) {
        this.repository = repository;
        this.dirstate = dirstate;
    }

    @Override
    public LocalRepository getRepository() {
        return repository;
    }

    @Override
    public String getParentChangesetID() {
        return dirstate.getPrimaryParent();
    }

    @Override
    public String getSecondaryChangesetID() {
        return dirstate.getSecondaryParent();
    }

    @Override
    public WorkDirState scanForChanges(Collection<String> paths) throws PbException {
        return dirstate.scanDir();
    }

    @Override
    public void add(Collection<String> paths) throws PbException {
        for (String path : paths) {
            dirstate.setAdded(path);
        }
        try {
            dirstate.write();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }

    public void copy(String oldPath, String newPath) throws PbException {
        try {
            Utils.copyFiles(new File(repository.getPath(), oldPath), new File(repository.getPath(), newPath));
        } catch (IOException ex) {
            throw new PbException("Failed to copy", ex);
        }
        dirstate.setAdded(newPath);
        try {
            dirstate.write();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }

    @Override
    public void move(String oldPath, String newPath) throws PbException {
        boolean success = new File(repository.getPath(), oldPath).renameTo(
                new File(repository.getPath(), newPath));
        if (!success) {
            throw new PbException("Failed to move " + oldPath + " to " + newPath);
        }
        dirstate.setRemoved(oldPath);
        dirstate.setAdded(newPath);
        dirstate.setCopyOf(oldPath, newPath);
        try {
            dirstate.write();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }

    @Override
    public void remove(Collection<String> paths) throws PbException {
        for (String path : paths) {
            boolean success = new File(repository.getPath(), path).delete();
            if (!success) {
                throw new PbException("Failed to delete " + path);
            }
            dirstate.setRemoved(path);
        }
        try {
            dirstate.write();
        } catch (IOException ex) {
            throw new PbException("Failed to write dirstate", ex);
        }
    }

    @Override
    public void commit(String author, String message, Collection<String> paths) throws PbException {
        throw new UnsupportedOperationException();
    }
}
