package org.saturnine.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import org.saturnine.api.PbException;
import org.saturnine.api.Repository;
import org.saturnine.api.Changeset;
import org.saturnine.util.FileUtil;

/**
 *
 * @author Alexey Vladykin
 */
public class LocalRepository implements Repository {

    /*package*/ static final String DOT_PB = ".pb";
    /*package*/ static final String CHANGESETS = "changesets";
    /*package*/ static final String STATES = "states";
    private static final String PBRC = "pbrc";
    private static final String DIRSTATE = "dirstate";

    /**
     * Parent repository URL.
     */
    public static final String PROP_PARENT = "parent";

    public static LocalRepository create(File dir) throws PbException {
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

            File statesDir = new File(metadataDir, STATES);
            if (!statesDir.mkdir()) {
                throw new PbException("Failed to create " + statesDir);
            }

            return new LocalRepository(dir);
        }
    }

    public static LocalRepository createClone(LocalRepository parent, File dir) throws PbException {
        if (dir.exists()) {
            throw new PbException(dir + " already exists");
        }

        if (!dir.mkdir()) {
            throw new PbException("Failed to create " + dir);
        }

        try {
            FileUtil.copyFiles(parent.dir, dir);
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

        return new LocalRepository(dir);
    }

    public static LocalRepository open(File dir) throws PbException {
        return new LocalRepository(dir);
    }

    public static LocalRepository find(File dir) throws PbException {
        while (dir != null) {
            if (new File(dir, DOT_PB).exists()) {
                return LocalRepository.open(dir);
            }
            dir = dir.getParentFile();
        }
        throw new PbException("No repository found");
    }

    private final File dir;
    private DirState dirstate;
    private Changelog changelog;

    private LocalRepository(File dir) {
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

    public DirState getDirState() throws PbException {
        if (dirstate == null) {
            try {
                dirstate = DirState.open(metadataFile(DIRSTATE), dir);
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }
        return dirstate;
    }

    private Changelog getChangesetDAG() throws PbException {
        if (changelog == null) {
            try {
                changelog = Changelog.open(metadataFile(CHANGESETS));
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }
        return changelog;
    }

    @Override
    public Collection<Changeset> getHeads() throws PbException {
        try {
            return getChangesetDAG().getHeads();
        } catch (IOException ex) {
            throw new PbException("IOException", ex);
        }
    }

    @Override
    public Changeset getChangeset(String changesetID) throws PbException {
        try {
            return getChangesetDAG().findChangeset(changesetID);
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
            return !DOT_PB.equals(name);
        }
    }
}
