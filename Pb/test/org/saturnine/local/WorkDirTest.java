package org.saturnine.local;

import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Rule;
import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Alexey Vladykin
 */
public class WorkDirTest {

    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();
    private File basedir;
    private File dirstate;
    private WorkDir workdir;

    @Before
    public void setUp() throws Exception {
        dirstate = tempDir.newFile("dirstate");
        basedir = tempDir.newFolder("basedir");
        workdir = WorkDir.create(dirstate, basedir);
    }

    @Test
    public void testEmpty() throws Exception {
        DirScanResult c = workdir.scan();
        assertThat(c.getAddedFiles().entrySet(), Matchers.<Map.Entry<String, String>>empty());
        assertThat(c.getRemovedFiles(), Matchers.<String>empty());
        assertThat(c.getCleanFiles(), Matchers.<String>empty());
        assertThat(c.getMissingFiles(), Matchers.<String>empty());
        assertThat(c.getModifiedFiles(), Matchers.<String>empty());
        assertThat(c.getUncertainFiles(), Matchers.<String>empty());
        assertThat(c.getUntrackedFiles(), Matchers.<String>empty());
    }

    @Test
    public void testAdd() throws Exception {
        new File(basedir, "foo").createNewFile();

        DirScanResult c1 = workdir.scan();
        assertThat(c1.getUntrackedFiles(), hasItem("foo"));
        assertThat(c1.getUntrackedFiles(), hasSize(1));
        assertThat(c1.getAddedFiles().entrySet(), Matchers.<Map.Entry<String, String>>empty());

        workdir.addFiles(Collections.singleton("foo"));

        DirScanResult c2 = workdir.scan();
        assertThat(c2.getUntrackedFiles(), Matchers.<String>empty());
        assertThat(c2.getAddedFiles(), hasEntry("foo", null));
        assertThat(c2.getAddedFiles().entrySet(), hasSize(1));
    }

    @Test
    public void testDelete() throws Exception {
        DirState.State state = new DirState.State();
        state.knownFiles().put("foo", new DirState.FileAttrs((short)0644, 35, 5));
        workdir.setState(state);

        DirScanResult c1 = workdir.scan();
        assertThat(c1.getMissingFiles(), hasItem("foo"));
        assertThat(c1.getMissingFiles(), hasSize(1));
        assertThat(c1.getRemovedFiles(), Matchers.<String>empty());

        workdir.removeFiles(Collections.singleton("foo"));

        DirScanResult c2 = workdir.scan();
        assertThat(c2.getMissingFiles(), Matchers.<String>empty());
        assertThat(c2.getRemovedFiles(), hasItem("foo"));
        assertThat(c2.getRemovedFiles(), hasSize(1));
    }
}
