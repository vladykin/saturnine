package org.saturnine.local;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.api.FileInfo;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DirStateTest {

    private FakeFile basedir;
    private File dirstateFile;
    private DirState dirstate;

    @Before
    public void setUp() throws Exception {
        basedir = new FakeFile("/tmp/foo");
        dirstateFile = new File("build/test/dirstate");
        dirstate = DirState.create(dirstateFile, basedir);
    }

    public void tearDown() throws Exception {
        dirstateFile.delete();
    }

    @Test
    public void testEmpty() throws Exception {
        DirState.Snapshot snapshot = dirstate.snapshot();
        assertEquals(Collections.emptySet(), snapshot.addedFiles());
        assertEquals(Collections.emptySet(), snapshot.removedFiles());

        DirScanResult c = snapshot.scanDir();
        assertEquals(Collections.emptySet(), c.getAddedFiles());
        assertEquals(Collections.emptySet(), c.getRemovedFiles());
        assertEquals(Collections.emptySet(), c.getCleanFiles());
        assertEquals(Collections.emptySet(), c.getMissingFiles());
        assertEquals(Collections.emptySet(), c.getModifiedFiles());
        assertEquals(Collections.emptySet(), c.getUncertainFiles());
        assertEquals(Collections.emptySet(), c.getUntrackedFiles());
    }

    @Test
    public void testAdd() throws Exception {
        basedir.addChildFile("foo", 5, 35);

        DirScanResult c1 = dirstate.snapshot().scanDir();
        assertEquals(Collections.singleton("foo"), c1.getUntrackedFiles());
        assertEquals(Collections.emptySet(), c1.getAddedFiles());

        DirState.Builder builder = dirstate.newBuilder(true);
        builder.addedFiles(Collections.singleton("foo"));

        DirScanResult c2 = builder.close().scanDir();
        assertEquals(Collections.emptySet(), c2.getUntrackedFiles());
        assertEquals(Collections.singleton("foo"), c2.getAddedFiles());
    }

    @Test
    public void testDelete() throws Exception {
        DirState.Builder builder1 = dirstate.newBuilder(false);
        builder1.knownFiles(Collections.singletonMap("foo", new FileInfo("foo", 5, (short)0644, 35, "0000000")));
        builder1.close();

        DirScanResult c1 = dirstate.snapshot().scanDir();
        assertEquals(Collections.singleton("foo"), c1.getMissingFiles());
        assertEquals(Collections.emptySet(), c1.getRemovedFiles());

        DirState.Builder builder2 = dirstate.newBuilder(true);
        builder2.removedFiles(Collections.singleton("foo"));
        builder2.close();

        DirScanResult c2 = dirstate.snapshot().scanDir();
        assertEquals(Collections.emptySet(), c2.getMissingFiles());
        assertEquals(Collections.singleton("foo"), c2.getRemovedFiles());
    }
}
