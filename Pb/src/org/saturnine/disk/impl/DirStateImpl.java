package org.saturnine.disk.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.saturnine.api.DirState;
import org.saturnine.api.FileChange;
import org.saturnine.api.FileChangeType;
import org.saturnine.api.FileState;
import org.saturnine.api.PbException;
import org.saturnine.util.Utils;

/**
 * @author Alexey Vladykin
 */
public final class DirStateImpl implements DirState {

    private static final String DIRSTATE = "dirstate";
    private static final char NEWLINE = '\n';

    /*package*/ static void init(File dir, String parentChangeset, String secondaryChangeset) throws IOException {
        File dirstateFile = new File(dir, DIRSTATE);
        BufferedWriter dirstateWriter = new BufferedWriter(new FileWriter(dirstateFile));
        try {
            dirstateWriter.append(parentChangeset).append(NEWLINE);
            dirstateWriter.append(secondaryChangeset).append(NEWLINE);

            // TODO: use API instead of reading file
            File stateFile = new File(dir, DiskRepository.STATES + "/" + parentChangeset);
            BufferedReader stateReader = new BufferedReader(new FileReader(stateFile));
            try {
                String line;
                while ((line = stateReader.readLine()) != null) {
                    dirstateWriter.append(line).append(NEWLINE);
                }
            } finally {
                stateReader.close();
            }

        } finally {
            dirstateWriter.close();
        }
    }

    /*package*/ static DirStateImpl read(DiskRepository repository) throws IOException {
        File dirstateFile = repository.metadataFile(DIRSTATE);
        BufferedReader dirstateReader = new BufferedReader(new FileReader(dirstateFile));
        try {
            String primaryChangeset = dirstateReader.readLine();
            String secondaryChangeset = dirstateReader.readLine();

            List<FileState> fileStates = new ArrayList<FileState>();
            while (true) {
                String line = dirstateReader.readLine();
                if (line == null || line.length() == 0) {
                    break;
                } else {
                    fileStates.add(FileStateImpl.parse(line));
                }
            }

            List<FileChange> fileChanges = new ArrayList<FileChange>();
            while (true) {
                String typeStr = dirstateReader.readLine();
                if (typeStr == null) {
                    break;
                }
                FileChangeType type = FileChangeType.valueOf(typeStr);
                String path = dirstateReader.readLine();
                if (path == null) {
                    break;
                }
                String copyOf = null;
                if (type == FileChangeType.ADD) {
                    copyOf = dirstateReader.readLine();
                    if ("<null>".equals(copyOf)) {
                        copyOf = null;
                    }
                }
                fileChanges.add(new FileChangeImpl(path, type, copyOf));
            }

            return new DirStateImpl(repository,
                    primaryChangeset, secondaryChangeset,
                    fileStates, fileChanges);
        } finally {
            dirstateReader.close();
        }
    }

    private final DiskRepository repository;
    private final String primaryChangeset;
    private final String secondaryChangeset;
    private final List<FileState> fileStates;
    private final List<FileChange> fileChanges;

    private DirStateImpl(DiskRepository repository,
            String primaryChangeset, String secondaryChangeset,
            List<FileState> fileStates, List<FileChange> fileChanges) {
        this.repository = repository;
        this.primaryChangeset = primaryChangeset;
        this.secondaryChangeset = secondaryChangeset;
        this.fileStates = fileStates;
        this.fileChanges = fileChanges;
    }

    @Override
    public DiskRepository getRepository() {
        return repository;
    }

    @Override
    public String getParentChangesetID() {
        return primaryChangeset;
    }

    @Override
    public String getSecondaryChangesetID() {
        return secondaryChangeset;
    }

    @Override
    public List<FileChange> getWorkDirChanges(Collection<String> paths) throws PbException {
        List<FileChange> changes = new ArrayList<FileChange>(fileChanges);

        List<FileState> actualStates = readFileStatesFromDisk();
        List<FileState> expectedStates = fileStates;

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
                writer.write(String.valueOf(state.getSize()) + " " + String.valueOf(state.getLastModified()) + " " + state.getPath() + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }


    private List<FileState> readFileStatesFromDisk() {
        List<FileState> states = new ArrayList<FileState>();
        collectFileStates(repository.getPath(), states);
        return states;
    }

    private void collectFileStates(File dir, List<FileState> states) {
        File[] children = dir.listFiles(new DiskRepository.PbFileFilter());
        for (File f : children) {
            if (f.isDirectory()) {
                collectFileStates(f, states);
            } else {
                states.add(new FileStateImpl(Utils.relativePath(repository.getPath(), f), f.length(), 0));
            }
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
}
