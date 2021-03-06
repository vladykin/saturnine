package org.saturnine.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.saturnine.api.PbException;
import org.saturnine.api.Repository;

/**
 *
 * @author Alexey Vladykin
 */
public class LocalRepository implements Repository {

    /*package*/ static final String DOT_PB = ".pb";
    /*package*/ static final String CHANGELOG = "changelog";
    /*package*/ static final String DIRLOG = "dirlog";
    private static final String PBRC = "pbrc";
    private static final String DIRSTATE = "dirstate";

    /**
     * Parent repository URL.
     */
    public static final String PROP_PARENT = "parent";

    /**
     * User name to use as commit author.
     */
    public static final String PROP_USER = "user";

    public static LocalRepository create(File dir) throws PbException {
        dir.mkdirs();

        if (!dir.isDirectory()) {
            throw new PbException("Failed to open directory " + dir);
        } else {
            File metadataDir = new File(dir, DOT_PB);
            if (!metadataDir.mkdir()) {
                throw new PbException("Failed to create " + metadataDir);
            }

            File changelogFile = new File(metadataDir, CHANGELOG);
            try {
                Changelog changelog = Changelog.create(changelogFile);
            } catch (IOException ex) {
                throw new PbException("Failed to create " + changelogFile);
            }

            File dirlogFile = new File(metadataDir, DIRLOG);
            try {
                Dirlog dirlog = Dirlog.create(dirlogFile);
            } catch (IOException ex) {
                throw new PbException("Failed to create " + dirlogFile);
            }

            File dirstateFile = new File(metadataDir, DIRSTATE);
            try {
                WorkDir workdir = WorkDir.create(dirstateFile, dir);
            } catch (IOException ex) {
                throw new PbException("Failed to create " + dirstateFile);
            }

            File pbrcFile = new File(metadataDir, PBRC);
            try {
                pbrcFile.createNewFile();
            } catch (IOException ex) {
                throw new PbException("Failed to create " + pbrcFile);
            }

            return new LocalRepository(dir);
        }
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
    private Changelog changelog;
    private Dirlog dirlog;
    private WorkDir workdir;

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

    public WorkDir getWorkDir() throws PbException {
        if (workdir == null) {
            try {
                workdir = WorkDir.open(metadataFile(DIRSTATE), dir);
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }
        return workdir;
    }

    public Dirlog getDirlog() throws PbException {
        if (dirlog == null) {
            try {
                dirlog = Dirlog.open(metadataFile(DIRLOG));
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }
        return dirlog;
    }

    public Changelog getChangelog() throws PbException {
        if (changelog == null) {
            try {
                changelog = Changelog.open(metadataFile(CHANGELOG));
            } catch (IOException ex) {
                throw new PbException("IOException", ex);
            }
        }
        return changelog;
    }
}
