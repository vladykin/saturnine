package org.saturnine.disk.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirState;
import org.saturnine.api.FileChange;
import org.saturnine.api.FileChangeType;
import org.saturnine.api.FileState;
import org.saturnine.api.PbException;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public class DirStateImpl implements DirState {


    private final DiskRepository repository;

    /*package*/ DirStateImpl(DiskRepository repository) {
        this.repository = repository;
    }

    public DiskRepository getRepository() {
        return repository;
    }

    @Override
    public Collection<String> getParentChangesetIDs() {
        String line;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(repository.metadataFile(DiskRepository.CURRENT_ID)));
            try {
                line = reader.readLine();
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            line = null;
        }
        return (line == null || line.length() == 0) ?
            Collections.<String>emptyList() :
            Collections.<String>singletonList(line);
    }

    @Override
    public List<FileChange> getWorkDirChanges(Collection<String> paths) throws PbException {
        List<FileChange> changes = new ArrayList<FileChange>(readApprovedChanges());

        List<FileState> actualStates = readFileStatesFromDisk();
        List<FileState> expectedStates = readFileStatesFromMetadata();

        for (FileChange change : changes) {
            removeRelated(change, actualStates);
            removeRelated(change, expectedStates);
        }

        Map<String, FileState> statesMap = new HashMap<String, FileState>();
        for (FileState state : expectedStates) {
            statesMap.put(state.getPath(), state);
        }

        for (FileState actualState : actualStates) {
            FileState origState = statesMap.get(actualState.getPath());
            if (origState == null) {
                changes.add(new FileChangeImpl(actualState.getPath(), FileChangeType.ADD, null));
            } else if (!origState.equals(actualState)) {
                changes.add(new FileChangeImpl(actualState.getPath(), FileChangeType.MODIFY, null));
            }
        }

        for (Map.Entry<String, FileState> entry : statesMap.entrySet()) {
            boolean found = false;
            for (FileState state : actualStates) {
                if (entry.getKey().equals(state.getPath())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                changes.add(new FileChangeImpl(entry.getValue().getPath(), FileChangeType.REMOVE, null));
            }
        }

        return changes;
    }
    @Override
    public boolean isAboutToAdd(String path) throws PbException {
        return true;
    }

    @Override
    public void add(Collection<String> paths) throws PbException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void move(String oldPath, String newPath) throws PbException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isAboutToRemove(String path) throws PbException {
        return true;
    }

    @Override
    public void remove(Collection<String> path) throws PbException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commit(String author, String message, Collection<String> paths) throws PbException {
        List<FileChange> changes = getWorkDirChanges(paths);
        DiskChangeset changeset = DiskChangeset.create(this, author, message, changes);
        try {
            FileWriter writer = new FileWriter(repository.metadataFile(DiskRepository.CURRENT_ID));
            writer.write(changeset.getID());
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }

        List<FileState> actualStates = readFileStatesFromDisk();
        try {
            FileWriter writer = new FileWriter(repository.metadataFile(DiskRepository.STATES + "/" + changeset.getID()));
            for (FileState state : actualStates) {
                writer.write(String.valueOf(state.getSize()) + " " + String.valueOf(state.getTimeModified().getTime()) + " " + state.getPath() + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }


    private List<FileChange> readApprovedChanges() {
        File approvedFile = repository.metadataFile(DiskRepository.APPROVED);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(approvedFile));
            try {
                List<FileChange> changes = new ArrayList<FileChange>();
                while (true) {
                    String typeStr = reader.readLine();
                    if (typeStr == null) {
                        break;
                    }
                    FileChangeType type = FileChangeType.valueOf(typeStr);
                    String path = reader.readLine();
                    if (path == null) {
                        break;
                    }
                    String copyOf = null;
                    if (type == FileChangeType.ADD) {
                        copyOf = reader.readLine();
                        if ("<null>".equals(copyOf)) {
                            copyOf = null;
                        }
                    }
                    changes.add(new FileChangeImpl(path, type, copyOf));
                }
                return changes;
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            if (ex instanceof FileNotFoundException) {
                return Collections.emptyList();
            } else {
                ex.printStackTrace();
                return null;
            }
        }
    }

    private List<FileState> readFileStatesFromDisk() {
        List<FileState> states = new ArrayList<FileState>();
        collectFileStates(repository.getPath(), states);
        return states;
    }

    private void collectFileStates(File dir, List<FileState> states) {
        File[] children = dir.listFiles(new FilenameFilterImpl());
        for (File f : children) {
            if (f.isDirectory()) {
                collectFileStates(f, states);
            } else {
                states.add(new FileStateImpl(Utils.relativePath(repository.getPath(), f), f.length(), new Date(0)));
            }
        }
    }

    private List<FileState> readFileStatesFromMetadata() throws PbException {
        String changesetID = getParentChangesetIDs().iterator().next();
        if (Changeset.NULL_ID.equals(changesetID)) {
            return Collections.emptyList();
        }

        File stateFile = repository.metadataFile(DiskRepository.STATES + "/" + changesetID);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(stateFile));
            try {
                String line;
                List<FileState> states = new ArrayList<FileState>();
                while ((line = reader.readLine()) != null) {
                    states.add(FileStateImpl.parse(line));
                }
                return states;
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    private static List<FileState> removeRelated(FileChange change, List<FileState> states) {
        List<FileState> result = new ArrayList<FileState>();
        for (FileState state : states) {
            if (state.getPath().equals(change.getPath()) || state.getPath().equals(change.getCopyOf())) {
                result.add(state);
            }
        }
        return result;
    }

    private static class FilenameFilterImpl implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return !DiskRepository.DOT_PB.equals(name);
        }
    }
}
