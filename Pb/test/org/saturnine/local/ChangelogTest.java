package org.saturnine.local;

import org.hamcrest.Matchers;
import org.junit.Rule;
import java.io.File;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.saturnine.api.Changeset;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class ChangelogTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File changelogFile;
    private Changelog changelog;

    @Before
    public void setUp() throws Exception {
        changelogFile = tempFolder.newFile("changelog");
        changelog = Changelog.create(changelogFile);
    }

    @Test
    public void testGetHeads() throws Exception {
        assertThat(changelog.getHeads(), Matchers.<Changeset>empty());

        Changelog.Builder builder = changelog.newBuilder();
        builder.id("1234567890123456789012345678901234567890");
        builder.primaryParent(Changeset.NULL).author("Alexey").comment("test");
        Changeset changeset = builder.writeChangeset();
        builder.close();

        Collection<Changeset> heads = changelog.getHeads();
        assertThat(heads, hasItem(changeset));
        assertThat(heads, hasSize(1));
    }
}
