package org.saturnine.local;

import org.hamcrest.Matchers;
import org.junit.rules.TemporaryFolder;
import org.junit.Rule;
import java.io.File;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DirlogTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File dirlogFile;
    private Dirlog dirlog;

    @Before
    public void setUp() throws Exception {
        dirlogFile = tempFolder.newFile("dirlog");
        dirlog = Dirlog.create(dirlogFile);
    }

    @Test
    public void testAppend() throws Exception {
        DirDiff diff1 = null;
        Dirlog.Builder builder = dirlog.newBuilder();
        try {
            builder.newState("0123456789012345678901234567890123456789");
            builder.oldState(Changeset.NULL);
            builder.addedFile(new FileInfo("foo", 123, (short) 0644, "12345678"));
            diff1 = builder.writeDiff();
        } finally {
            builder.close();
        }

        DirDiff diff2 = null;
        Dirlog.Reader reader = dirlog.newReader();
        try {
            diff2 = reader.next();
        } finally {
            reader.close();
        }

        assertThat(diff2.newState(), equalTo(diff1.newState()));
        assertThat(diff2.oldState(), equalTo(diff1.oldState()));
    }

    @Test
    public void testState() throws Exception {
        Dirlog.Builder builder = dirlog.newBuilder();
        try {
            builder.newState("0123456789012345678901234567890123456789");
            builder.oldState(Changeset.NULL);
            builder.addedFile(new FileInfo("foo", 123, (short) 0644, "12345678"));
            builder.writeDiff();

            builder.newState("1234567890123456789012345678901234567890");
            builder.oldState("0123456789012345678901234567890123456789");
            builder.modifiedFile(new FileInfo("foo", 234, (short) 0644, "87654321"));
            builder.writeDiff();

            builder.newState("2345678901234567890123456789012345678901");
            builder.oldState("1234567890123456789012345678901234567890");
            builder.removedFile("foo");
            builder.writeDiff();
        } finally {
            builder.close();
        }

        Map<String, FileInfo> state1 = dirlog.state("0123456789012345678901234567890123456789");
        assertThat(state1, hasEntry("foo", new FileInfo("foo", 123, (short) 0644, "12345678")));
        assertThat(state1.entrySet(), hasSize(1));

        Map<String, FileInfo> state2 = dirlog.state("1234567890123456789012345678901234567890");
        assertThat(state2, hasEntry("foo", new FileInfo("foo", 234, (short) 0644, "87654321")));
        assertThat(state2.entrySet(), hasSize(1));

        Map<String, FileInfo> state3 = dirlog.state("2345678901234567890123456789012345678901");
        assertThat(state3.entrySet(), Matchers.<Map.Entry<String, FileInfo>>empty());
    }
}
