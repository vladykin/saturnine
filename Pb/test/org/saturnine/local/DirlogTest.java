package org.saturnine.local;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.saturnine.api.Changeset;
import org.saturnine.api.DirDiff;
import org.saturnine.api.FileInfo;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class DirlogTest {

    private File dirlogFile;
    private Dirlog dirlog;

    @Before
    public void setUp() throws Exception {
        dirlogFile = new File("build/test/dirlog");
        dirlog = Dirlog.create(dirlogFile);
    }

    @After
    public void tearDown() throws Exception {
        dirlogFile.delete();
    }

    @Test
    public void testAppend() throws Exception {
        DirDiff diff1 = null;
        Dirlog.Builder builder = dirlog.newBuilder();
        try {
            builder.newState("0123456789012345678901234567890123456789");
            builder.oldState(Changeset.NULL);
            builder.addedFiles(Collections.singletonMap("foo",
                    new FileInfo("foo", 123, (short) 0644, new Date().getTime(), "qwerqwer")));
            diff1 = builder.closeDiff();
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

        assertEquals(diff1.newState(), diff2.newState());
        assertEquals(diff1.oldState(), diff2.oldState());
    }

    @Test
    public void testState() throws Exception {
        Dirlog.Builder builder = dirlog.newBuilder();
        try {
            builder.newState("0123456789012345678901234567890123456789");
            builder.oldState(Changeset.NULL);
            builder.addedFiles(Collections.singletonMap("foo",
                    new FileInfo("foo", 123, (short) 0644, 1000, "qwerqwer")));
            builder.closeDiff();

            builder.newState("1234567890123456789012345678901234567890");
            builder.oldState("0123456789012345678901234567890123456789");
            builder.modifiedFiles(Collections.singletonMap("foo",
                    new FileInfo("foo", 234, (short) 0644, 2000, "asdfasdf")));
            builder.closeDiff();

            builder.newState("2345678901234567890123456789012345678901");
            builder.oldState("1234567890123456789012345678901234567890");
            builder.removedFiles(Collections.singleton("foo"));
            builder.closeDiff();
        } finally {
            builder.close();
        }

        Map<String, FileInfo> state1 = dirlog.state("0123456789012345678901234567890123456789");
        assertEquals(1, state1.size());
        assertEquals(new FileInfo("foo", 123, (short) 0644, 1000, "qwerqwer"), state1.get("foo"));

        Map<String, FileInfo> state2 = dirlog.state("1234567890123456789012345678901234567890");
        assertEquals(1, state2.size());
        assertEquals(new FileInfo("foo", 234, (short) 0644, 2000, "asdfasdf"), state2.get("foo"));

        Map<String, FileInfo> state3 = dirlog.state("2345678901234567890123456789012345678901");
        assertEquals(0, state3.size());
    }
}
