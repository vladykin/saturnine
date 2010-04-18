package org.saturnine.local;

import java.io.File;
import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.api.Changeset;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class ChangelogTest {

    private File changelogFile;
    private Changelog changelog;

    public ChangelogTest() {
    }

    @Before
    public void setUp() throws Exception {
        changelogFile = new File("build/test/changelog.test");
        changelog = new Changelog(changelogFile);
    }

    @After
    public void tearDown() throws Exception {
        changelogFile.delete();
    }

    @Test
    public void testGetHeads() throws Exception {
        assertTrue(changelog.getHeads().isEmpty());

        Changelog.Builder b = changelog.newChangesetBuilder();
        b.primaryParent(Changeset.NULL).author("Alexey").comment("test");
        String id = b.add();
        b.commit();

        Collection<Changeset> heads = changelog.getHeads();
        assertEquals(1, heads.size());
        Changeset head = heads.iterator().next();
        assertEquals(id, head.id());
        assertEquals(Changeset.NULL, head.primaryParent());
        assertNull(head.secondaryParent());
        assertEquals("Alexey", head.author());
        assertEquals("test", head.comment());
        assertNotNull(head.timestamp());
    }
}
