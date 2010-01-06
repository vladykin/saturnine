package org.saturnine.disk.impl;

import java.io.File;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.api.FileState;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DirStateImpl2Test {

    private FakeFile basedir;

    @Before
    public void setUp() {
        basedir = new FakeFile("/tmp/foo");
    }

    @Test
    public void testEmpty() {
        DirStateImpl2 dirstate = DirStateImpl2.create(
                new File("boo"), basedir, Collections.<String, FileState>emptyMap());
        assertEquals(Collections.emptySet(), dirstate.getAddedFiles());
        assertEquals(Collections.emptySet(), dirstate.getRemovedFiles());

        DirStateImpl2.Snapshot s = dirstate.createSnapshot();
        assertEquals(Collections.emptySet(), s.getAddedFiles());
        assertEquals(Collections.emptySet(), s.getRemovedFiles());
        assertEquals(Collections.emptySet(), s.getCleanFiles());
        assertEquals(Collections.emptySet(), s.getMissingFiles());
        assertEquals(Collections.emptySet(), s.getModifiedFiles());
        assertEquals(Collections.emptySet(), s.getUncertainFiles());
        assertEquals(Collections.emptySet(), s.getUntrackedFiles());
    }

    @Test
    public void testAdd() {
        basedir.addChildFile("foo", 5, 35);

        DirStateImpl2 dirstate = DirStateImpl2.create(
                new File("boo"), basedir, Collections.<String, FileState>emptyMap());

        DirStateImpl2.Snapshot s1 = dirstate.createSnapshot();
        assertEquals(Collections.singleton("foo"), s1.getUntrackedFiles());
        assertEquals(Collections.emptySet(), s1.getAddedFiles());

        dirstate.setAdded("foo");
        DirStateImpl2.Snapshot s2 = dirstate.createSnapshot();
        assertEquals(Collections.emptySet(), s2.getUntrackedFiles());
        assertEquals(Collections.singleton("foo"), s2.getAddedFiles());
    }

    @Test
    public void testDelete() {
        DirStateImpl2 dirstate = DirStateImpl2.create(
                new File("boo"), basedir, Collections.<String, FileState>singletonMap(
                "foo", new FileStateImpl("foo", 5, 35)));

        DirStateImpl2.Snapshot s1 = dirstate.createSnapshot();
        assertEquals(Collections.singleton("foo"), s1.getMissingFiles());
        assertEquals(Collections.emptySet(), s1.getRemovedFiles());

        dirstate.setRemoved("foo");
        DirStateImpl2.Snapshot s2 = dirstate.createSnapshot();
        assertEquals(Collections.emptySet(), s2.getMissingFiles());
        assertEquals(Collections.singleton("foo"), s2.getRemovedFiles());
    }
}
