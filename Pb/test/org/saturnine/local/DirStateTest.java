package org.saturnine.local;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.api.Changeset;
import org.saturnine.api.WorkDirState;
import org.saturnine.api.FileState;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DirStateTest {

    private FakeFile basedir;

    @Before
    public void setUp() {
        basedir = new FakeFile("/tmp/foo");
    }

    @Test
    public void testEmpty() {
        DirState dirstate = DirState.create(
                new File("boo"), basedir,
                Changeset.NULL_ID, Changeset.NULL_ID,
                Collections.<String, FileState>emptyMap());
        assertEquals(Collections.emptySet(), dirstate.getAddedFiles());
        assertEquals(Collections.emptySet(), dirstate.getRemovedFiles());

        WorkDirState c = dirstate.scanDir();
        assertEquals(Collections.emptySet(), c.getAddedFiles());
        assertEquals(Collections.emptySet(), c.getRemovedFiles());
        assertEquals(Collections.emptySet(), c.getCleanFiles());
        assertEquals(Collections.emptySet(), c.getMissingFiles());
        assertEquals(Collections.emptySet(), c.getModifiedFiles());
        assertEquals(Collections.emptySet(), c.getUncertainFiles());
        assertEquals(Collections.emptySet(), c.getUntrackedFiles());
    }

    @Test
    public void testAdd() {
        basedir.addChildFile("foo", 5, 35);

        DirState dirstate = DirState.create(
                new File("boo"), basedir,
                Changeset.NULL_ID, Changeset.NULL_ID,
                Collections.<String, FileState>emptyMap());

        WorkDirState c1 = dirstate.scanDir();
        assertEquals(Collections.singleton("foo"), c1.getUntrackedFiles());
        assertEquals(Collections.emptySet(), c1.getAddedFiles());

        dirstate.setAdded("foo");
        WorkDirState c2 = dirstate.scanDir();
        assertEquals(Collections.emptySet(), c2.getUntrackedFiles());
        assertEquals(Collections.singleton("foo"), c2.getAddedFiles());
    }

    @Test
    public void testDelete() {
        DirState dirstate = DirState.create(
                new File("boo"), basedir,
                Changeset.NULL_ID, Changeset.NULL_ID,
                Collections.<String, FileState>singletonMap("foo", new FileStateImpl("foo", 5, 35)));

        WorkDirState c1 = dirstate.scanDir();
        assertEquals(Collections.singleton("foo"), c1.getMissingFiles());
        assertEquals(Collections.emptySet(), c1.getRemovedFiles());

        dirstate.setRemoved("foo");
        WorkDirState c2 = dirstate.scanDir();
        assertEquals(Collections.emptySet(), c2.getMissingFiles());
        assertEquals(Collections.singleton("foo"), c2.getRemovedFiles());
    }
}
