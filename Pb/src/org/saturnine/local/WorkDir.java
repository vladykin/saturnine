package org.saturnine.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.saturnine.local.DirState.FileAttrs;
import org.saturnine.util.FileUtil;

/**
 * @author Alexey Vladykin
 */
public class WorkDir {

    public static WorkDir open(File dirstate, File basedir) throws IOException {
        return new WorkDir(dirstate, basedir, false);
    }

    public static WorkDir create(File dirstate, File basedir) throws IOException {
        return new WorkDir(dirstate, basedir, true);
    }

    private final File basedir;
    private final DirState dirstate;

    private WorkDir(File dirstate, File basedir, boolean create) throws IOException {
        if (!basedir.isDirectory()) {
            throw new IOException(basedir.getPath());
        }
        this.basedir = basedir;
        this.dirstate = create? DirState.create(dirstate) : DirState.open(dirstate);
    }

    public void addFiles(Collection<String> paths) throws IOException {
        DirState.State state = dirstate.getState();
        for (String addedPath : paths) {
            state.removedFiles().remove(addedPath);
            state.addedFiles().put(addedPath, null);
        }
        dirstate.setState(state);
    }

    public void copyFiles(Collection<String> paths) throws IOException {
        if (paths.size() != 2) {
            throw new IllegalArgumentException("copyFiles supports only two arguments now");
        }
        Iterator<String> it = paths.iterator();
        String src = it.next();
        String dst = it.next();
        FileUtil.copyFiles(new File(basedir, src), new File(basedir, dst));

        DirState.State state = dirstate.getState();
        state.addedFiles().put(dst, src);
        dirstate.setState(state);
    }

    public void moveFiles(Collection<String> paths) throws IOException {
        if (paths.size() != 2) {
            throw new IllegalArgumentException("moveFiles supports only two arguments now");
        }
        Iterator<String> it = paths.iterator();
        String src = it.next();
        String dst = it.next();
        FileUtil.rename(new File(basedir, src), new File(basedir, dst));

        DirState.State state = dirstate.getState();
        state.removedFiles().add(src);
        state.addedFiles().put(dst, src);
        dirstate.setState(state);
    }

    public void removeFiles(Collection<String> paths) throws IOException {
        DirState.State state = dirstate.getState();
        for (String removedPath : paths) {
            FileUtil.delete(new File(basedir, removedPath));
            state.addedFiles().remove(removedPath);
            state.removedFiles().add(removedPath);
        }
        dirstate.setState(state);
    }

    public void recordFileAttrs(Collection<String> paths) throws IOException {
        // also clears all added/copied/removed info
        DirState.State state = new DirState.State();
        for (String path : paths) {
            File file = new File(basedir, path);
            if (!file.exists()) {
                throw new FileNotFoundException(file.getPath());
            }
            state.knownFiles().put(path, new FileAttrs((short) 0, file.length(), file.lastModified()));
        }
        dirstate.setState(state);
    }

    public DirScanResult scan() throws IOException {
        DirState.State state = dirstate.getState();

        Set<String> clean = new HashSet<String>();
        Set<String> modified = new HashSet<String>();
        Set<String> uncertain = new HashSet<String>();
        Set<String> untracked = new HashSet<String>();
        collectFileChanges(basedir, state, clean, modified, uncertain, untracked);

        Set<String> missing = new HashSet<String>(state.knownFiles().keySet());
        missing.removeAll(state.removedFiles());
        missing.removeAll(clean);
        missing.removeAll(modified);
        missing.removeAll(uncertain);

        return new DirScanResult(state.addedFiles(), state.removedFiles(), clean, missing, modified, uncertain, untracked);
    }

    /*package*/ void setState(DirState.State state) throws IOException {
        dirstate.setState(state);
    }

    private void collectFileChanges(File dir, DirState.State state, Set<String> clean, Set<String> modified, Set<String> uncertain, Set<String> untracked) {
        File[] children = dir.listFiles(new LocalRepository.PbFileFilter());
        for (File f : children) {
            if (f.isDirectory()) {
                collectFileChanges(f, state, clean, modified, uncertain, untracked);
            } else {
                String path = FileUtil.relativePath(basedir, f);
                FileAttrs oldAttrs = state.knownFiles().get(path);
                if (oldAttrs == null) {
                    if (!state.addedFiles().containsKey(path)) {
                        untracked.add(path);
                    }
                } else {
                    long newSize = f.length();
                    long newTimestamp = f.lastModified();
                    if (newSize != oldAttrs.size()) {
                        modified.add(path);
                    } else if (newTimestamp != oldAttrs.timestamp()) {
                        uncertain.add(path);
                    } else {
                        clean.add(path);
                    }
                }
            }
        }
    }
}