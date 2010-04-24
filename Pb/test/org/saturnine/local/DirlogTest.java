package org.saturnine.local;

import java.io.File;
import java.util.Collections;
import java.util.Date;
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
}
