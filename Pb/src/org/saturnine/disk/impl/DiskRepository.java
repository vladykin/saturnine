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
import org.saturnine.api.PbException;
import org.saturnine.api.Repository;
import org.saturnine.api.Changeset;
import org.saturnine.api.FileChange;
import org.saturnine.api.FileChangeType;
import org.saturnine.api.FileState;
import org.saturnine.util.Utils;

/**
 *
 * @author Alexey Vladykin
 */
public class DiskRepository implements Repository {

    private static final String DOT_PB = ".pb";
    /*package*/ static final String CURRENT_ID = "current_id";
    /*package*/ static final String CHANGESETS = "changesets";
    private static final String STATES = "states";
    private static final String APPROVED = "approved";
    private static final String PARENT = "parent";

    public static DiskRepository create(File dir) throws PbException {
        String[] content = dir.list();
        if (content == null) {
            throw new PbException("Failed to open directory " + dir);
        } else if (0 < content.length) {
            throw new PbException(dir + " is not empty");
        } else {
            File metadataDir = new File(dir, DOT_PB);
            if (!metadataDir.mkdir()) {
                throw new PbException("Failed to create " + metadataDir);
            }

            File currentIdFile = new File(metadataDir, CURRENT_ID);
            try {
                currentIdFile.createNewFile();
                FileWriter writer = new FileWriter(currentIdFile);
                writer.write(Changeset.NULL_ID + "\n");
                writer.close();
            } catch (IOException ex) {
                throw new PbException("Failed to create " + currentIdFile);
            }

            File parentFile = new File(metadataDir, PARENT);
            try {
                parentFile.createNewFile();
            } catch (IOException ex) {
                throw new PbException("Failed to create " + parentFile);
            }

            File changesetsDir = new File(metadataDir, CHANGESETS);
            if (!changesetsDir.mkdir()) {
                throw new PbException("Failed to create " + changesetsDir);
            }

            File statesDir = new File(metadataDir, STATES);
            if (!statesDir.mkdir()) {
                throw new PbException("Failed to create " + statesDir);
            }

            return new DiskRepository(dir);
        }
    }

    public static DiskRepository createClone(DiskRepository parent, File dir) throws PbException {
        if (dir.exists()) {
            throw new PbException(dir + " already exists");
        }

        if (!dir.mkdir()) {
            throw new PbException("Failed to create " + dir);
        }

        try {
            Utils.copyFiles(parent.dir, dir);
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }

        File parentFile = new File(dir, DOT_PB + "/" + PARENT);
        try {
            //parentFile.createNewFile();
            FileWriter writer = new FileWriter(parentFile);
            writer.write(parent.getPath() + "\n");
            writer.close();
        } catch (IOException ex) {
            throw new PbException("Failed to create " + parentFile);
        }

        return new DiskRepository(dir);
    }

    public static DiskRepository open(File dir) throws PbException {
        return new DiskRepository(dir);
    }

    public static DiskRepository find(File dir) throws PbException {
        while (dir != null) {
            if (new File(dir, DOT_PB).exists()) {
                return DiskRepository.open(dir);
            }
            dir = dir.getParentFile();
        }
        throw new PbException("No repository found");
    }
    private final File dir;

    private DiskRepository(File dir) {
        this.dir = dir;
    }

    @Override
    public String getPath() {
        return dir.getAbsolutePath();
    }

    @Override
    public String getParent() throws PbException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(metadataFile(PARENT)));
            try {
                String line = reader.readLine();
                return line == null || line.length() == 0? null : line;
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /*package*/ File metadataFile(String name) {
        return new File(dir, DOT_PB + "/" + name);
    }

    @Override
    public String getCurrentID() throws PbException {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(metadataFile(CURRENT_ID)));
            try {
                return reader.readLine();
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    @Override
    public String[] getHeadIDs() throws PbException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Changeset getChangeset(String changesetID) throws PbException {

        if (Changeset.NULL_ID.equals(changesetID)) {
            return new DiskChangeset(this);
        }

        File changesetFile = metadataFile(CHANGESETS + "/" + changesetID);
        if (!changesetFile.exists()) {
            return null;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(changesetFile));
            try {
                String parentID = reader.readLine();
                String author = reader.readLine();
                String comment = reader.readLine();
                String timestamp = reader.readLine();
                if (parentID != null && author != null && comment != null && timestamp != null) {
                    return new DiskChangeset(this, changesetID, Collections.singletonList(parentID), author, comment, new Date(Long.parseLong(timestamp)));
                } else {
                    throw new PbException("Corrupted changeset file for " + changesetID);
                }
            } finally {
                reader.close();
            }
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    private List<FileChange> readApprovedChanges() {
        File approvedFile = metadataFile(APPROVED);
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
        collectFileStates(dir, states);
        return states;
    }

    private void collectFileStates(File dir, List<FileState> states) {
        File[] children = dir.listFiles(new FilenameFilterImpl());
        for (File f : children) {
            if (f.isDirectory()) {
                collectFileStates(f, states);
            } else {
                states.add(new FileStateImpl(Utils.relativePath(this.dir, f), f.length(), new Date(0)));
            }
        }
    }

    private List<FileState> readFileStatesFromMetadata() throws PbException {
        String changesetID = getCurrentID();
        if (Changeset.NULL_ID.equals(changesetID)) {
            return Collections.emptyList();
        }

        File stateFile = metadataFile(STATES + "/" + changesetID);
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
            FileWriter writer = new FileWriter(metadataFile(CURRENT_ID));
            writer.write(changeset.getID());
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }

        List<FileState> actualStates = readFileStatesFromDisk();
        try {
            FileWriter writer = new FileWriter(metadataFile(STATES + "/" + changeset.getID()));
            for (FileState state : actualStates) {
                writer.write(String.valueOf(state.getSize()) + " " + String.valueOf(state.getTimeModified().getTime()) + " " + state.getPath() + "\n");
            }
            writer.close();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    @Override
    public void pull(Repository parent) throws PbException {

        if (!parent.getWorkDirChanges(null).isEmpty()) {
            throw new PbException("Uncommitted changes in reporte repository");
        }

        if (!getWorkDirChanges(null).isEmpty()) {
            throw new PbException("Uncommitted changes in local repository");
        }

        String thisHeadID = getCurrentID();
        if (parent.getChangeset(thisHeadID) == null) {
            throw new PbException("Local copy is newer than repository, can't pull");
        }

        String parentHeadID = parent.getCurrentID();
        if (this.getChangeset(parentHeadID) == null) {
            System.out.println("New changesets found, pulling");
            try {
                Utils.copyFiles(new File(parent.getPath()), dir);
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }


            File parentFile = metadataFile(PARENT);
            try {
                FileWriter writer = new FileWriter(parentFile);
                writer.write(parent.getPath() + "\n");
                writer.close();
            } catch (IOException ex) {
                throw new PbException("Failed to create " + parentFile);
            }


            List<FileChange> changes = getWorkDirChanges(null);
            for (FileChange change : changes) {
                if (change.getType() == FileChangeType.ADD) {
                    new File(dir, change.getPath()).delete();
                }
            }
        } else {
            System.out.println("Nothing to pull");
        }
    }

    @Override
    public void push(Repository parent) throws PbException {
        if (!parent.getWorkDirChanges(null).isEmpty()) {
            throw new PbException("Uncommitted changes in remote repository");
        }

        if (!getWorkDirChanges(null).isEmpty()) {
            throw new PbException("Uncommitted changes in local repository");
        }

        String parentHeadID = parent.getCurrentID();
        if (this.getChangeset(parentHeadID) == null) {
            throw new PbException("Repository is newer than local copy, can't push");
        }

        String parentParent = parent.getParent();

        String thisHeadID = getCurrentID();
        if (parent.getChangeset(thisHeadID) == null) {
            System.out.println("New changesets found, pushing");
            try {
                Utils.copyFiles(dir, new File(parent.getPath()));
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }

            File parentFile = ((DiskRepository) parent).metadataFile(PARENT);
            try {
                FileWriter writer = new FileWriter(parentFile);
                writer.write(parentParent + "\n");
                writer.close();
            } catch (IOException ex) {
                throw new PbException("Failed to create " + parentFile);
            }

            List<FileChange> changes = parent.getWorkDirChanges(null);
            for (FileChange change : changes) {
                if (change.getType() == FileChangeType.ADD) {
                    new File(parent.getPath(), change.getPath()).delete();
                }
            }
        } else {
            System.out.println("Nothing to push");
        }
    }

    private static class FilenameFilterImpl implements FilenameFilter {

        public boolean accept(File dir, String name) {
            return !DOT_PB.equals(name);
        }
    }
}
