package org.saturnine.local;

import java.io.File;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.util.FileUtil;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class WorkDirTest {

    private File basedir;
    private File dirstate;
    private WorkDir workdir;

    @Before
    public void setUp() throws Exception {
        dirstate = new File("build/test/dirstate");
        basedir = new File("build/test/workdir");
        basedir.mkdir();
        workdir = WorkDir.create(dirstate, basedir);
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.delete(dirstate);
        FileUtil.deleteRecursively(basedir);
    }

    @Test
    public void testEmpty() throws Exception {
        DirScanResult c = workdir.scan();
        assertEquals(Collections.emptyMap(), c.getAddedFiles());
        assertEquals(Collections.emptySet(), c.getRemovedFiles());
        assertEquals(Collections.emptySet(), c.getCleanFiles());
        assertEquals(Collections.emptySet(), c.getMissingFiles());
        assertEquals(Collections.emptySet(), c.getModifiedFiles());
        assertEquals(Collections.emptySet(), c.getUncertainFiles());
        assertEquals(Collections.emptySet(), c.getUntrackedFiles());
    }

    @Test
    public void testAdd() throws Exception {
        new File(basedir, "foo").createNewFile();

        DirScanResult c1 = workdir.scan();
        assertEquals(Collections.singleton("foo"), c1.getUntrackedFiles());
        assertEquals(Collections.emptyMap(), c1.getAddedFiles());

        workdir.addFiles(Collections.singleton("foo"));

        DirScanResult c2 = workdir.scan();
        assertEquals(Collections.emptySet(), c2.getUntrackedFiles());
        assertEquals(Collections.singletonMap("foo", null), c2.getAddedFiles());
    }

    @Test
    public void testDelete() throws Exception {
        DirState.State state = new DirState.State();
        state.knownFiles().put("foo", new DirState.FileAttrs((short)0644, 35, 5));
        workdir.setState(state);

        DirScanResult c1 = workdir.scan();
        assertEquals(Collections.singleton("foo"), c1.getMissingFiles());
        assertEquals(Collections.emptySet(), c1.getRemovedFiles());

        workdir.removeFiles(Collections.singleton("foo"));

        DirScanResult c2 = workdir.scan();
        assertEquals(Collections.emptySet(), c2.getMissingFiles());
        assertEquals(Collections.singleton("foo"), c2.getRemovedFiles());
    }
}
