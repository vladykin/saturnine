package org.saturnine.local;

import java.io.File;
import org.junit.rules.TemporaryFolder;
import org.junit.Rule;
import org.junit.Test;
import org.saturnine.api.PbException;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * @author Alexey Vladykin
 */
public class LocalRepositoryTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testFind() throws Exception {
        File repoDir = tempFolder.newFolder("repo");
        assertThat(LocalRepository.create(repoDir), notNullValue());
        assertThat(LocalRepository.find(repoDir), notNullValue());

        File repoSubDir = new File(repoDir, "subdir");
        assertThat(LocalRepository.find(repoSubDir), notNullValue());
    }

    @Test(expected = PbException.class)
    public void testFindFail() throws Exception {
        File repoDir = tempFolder.newFolder("repo");
        LocalRepository.find(repoDir);
    }
}
