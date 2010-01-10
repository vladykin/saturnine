package org.saturnine.disk.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import org.saturnine.api.PbException;
import org.saturnine.api.Repository;
import org.saturnine.api.Changeset;
import org.saturnine.api.WorkDir;
import org.saturnine.util.Utils;

/**
 *
 * @author Alexey Vladykin
 */
public class DiskRepository implements Repository {

    /*package*/ static final String DOT_PB = ".pb";
    /*package*/ static final String CHANGESETS = "changesets";
    /*package*/ static final String STATES = "states";
    private static final String PBRC = "pbrc";

    /**
     * Parent repository URL.
     */
    public static final String PROP_PARENT = "parent";

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

            File pbrcFile = new File(metadataDir, PBRC);
            try {
                pbrcFile.createNewFile();
            } catch (IOException ex) {
                throw new PbException("Failed to create " + pbrcFile);
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

        Properties props = new Properties();
        props.setProperty(PROP_PARENT, parent.getPath().getAbsolutePath());

        File pbrcFile = new File(dir, DOT_PB + "/" + PBRC);
        try {
            //parentFile.createNewFile();
            FileOutputStream out = new FileOutputStream(pbrcFile);
            props.store(out, null);
            out.close();
        } catch (IOException ex) {
            throw new PbException("Failed to create " + pbrcFile);
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
    private WorkDir workdir;

    private DiskRepository(File dir) {
        this.dir = dir;
    }

    @Override
    public String getURL() {
        return dir.getAbsolutePath();
    }

    public File getPath() {
        return dir;
    }

    /*package*/ File metadataFile(String name) {
        return new File(dir, DOT_PB + "/" + name);
    }

    @Override
    public String getProperty(String key) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(metadataFile(PBRC)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props.getProperty(key);
    }

    @Override
    public Collection<String> getHeadIDs() throws PbException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public WorkDir getWorkDir() throws PbException {
        if (workdir == null) {
            workdir = WorkDirImpl.create(this);
        }
        return workdir;
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

    @Override
    public void pull(Repository parent) throws PbException {
        throw new UnsupportedOperationException();
    }

    /*package*/ static class PbFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return !DiskRepository.DOT_PB.equals(name);
        }
    }
}
