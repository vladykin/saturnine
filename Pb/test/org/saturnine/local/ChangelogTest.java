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
        changelog = Changelog.create(changelogFile);
    }

    @After
    public void tearDown() throws Exception {
        changelogFile.delete();
    }

    @Test
    public void testGetHeads() throws Exception {
        assertTrue(changelog.getHeads().isEmpty());

        Changelog.Builder builder = changelog.newBuilder();
        builder.id("1234567890123456789012345678901234567890");
        builder.primaryParent(Changeset.NULL).author("Alexey").comment("test");
        Changeset changeset = builder.writeChangeset();
        builder.close();

        Collection<Changeset> heads = changelog.getHeads();
        assertEquals(1, heads.size());
        Changeset head = heads.iterator().next();
        assertEquals(changeset, head);
    }
}
