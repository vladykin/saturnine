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
        DirState dirstate = DirState.create(
                new File("boo"), basedir, Collections.<String, FileState>emptyMap());
        assertEquals(Collections.emptySet(), dirstate.getAddedFiles());
        assertEquals(Collections.emptySet(), dirstate.getRemovedFiles());

        DirState.Snapshot s = dirstate.createSnapshot();
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

        DirState dirstate = DirState.create(
                new File("boo"), basedir, Collections.<String, FileState>emptyMap());

        DirState.Snapshot s1 = dirstate.createSnapshot();
        assertEquals(Collections.singleton("foo"), s1.getUntrackedFiles());
        assertEquals(Collections.emptySet(), s1.getAddedFiles());

        dirstate.setAdded("foo");
        DirState.Snapshot s2 = dirstate.createSnapshot();
        assertEquals(Collections.emptySet(), s2.getUntrackedFiles());
        assertEquals(Collections.singleton("foo"), s2.getAddedFiles());
    }

    @Test
    public void testDelete() {
        DirState dirstate = DirState.create(
                new File("boo"), basedir, Collections.<String, FileState>singletonMap(
                "foo", new FileStateImpl("foo", 5, 35)));

        DirState.Snapshot s1 = dirstate.createSnapshot();
        assertEquals(Collections.singleton("foo"), s1.getMissingFiles());
        assertEquals(Collections.emptySet(), s1.getRemovedFiles());

        dirstate.setRemoved("foo");
        DirState.Snapshot s2 = dirstate.createSnapshot();
        assertEquals(Collections.emptySet(), s2.getMissingFiles());
        assertEquals(Collections.singleton("foo"), s2.getRemovedFiles());
    }
}
