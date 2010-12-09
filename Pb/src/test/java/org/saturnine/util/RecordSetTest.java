package org.saturnine.util;

import java.io.File;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class RecordSetTest {

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    private File recordsetFile;
    private RecordSet recordset;

    @Before
    public void setUp() throws Exception {
        recordsetFile = tempFolder.newFile("recordset");
        recordset = RecordSet.create(recordsetFile);
    }

    @Test
    public void testReaderNext() throws Exception {
        RecordSet.Reader reader = recordset.newReader();
        try {
            assertThat(reader, notNullValue());
            assertThat(reader.next(), is(false));
        } finally {
            reader.close();
        }

        RecordSet.Writer writer = recordset.newWriter();
        try {
            writer.writeRecord();
        } finally {
            writer.close();
        }

        reader = recordset.newReader();
        try {
            assertThat(reader.next(), is(true));
            assertThat(reader.next(), is(false));
        } finally {
            reader.close();
        }
    }

    @Test
    public void testReaderInputStream() throws Exception {
        RecordSet.Writer writer = recordset.newWriter();
        try {
            writer.outputStream().write(1);
            writer.outputStream().write(2);
            writer.outputStream().write(3);
            writer.outputStream().close();
            writer.writeRecord();
            writer.outputStream().write(3);
            writer.outputStream().write(2);
            writer.outputStream().write(1);
            writer.outputStream().close();
            writer.writeRecord();
        } finally {
            writer.close();
        }

        byte[] buf = new byte[4];
        RecordSet.Reader reader = recordset.newReader();
        try {
            assertThat(reader.next(), is(true));
            assertThat(reader.inputStream().read(buf), is(3));
            assertThat(buf, is(new byte[]{1, 2, 3, 0}));
            reader.inputStream().close();
            assertThat(reader.next(), is(true));
            assertThat(reader.inputStream().read(buf), is(3));
            assertThat(buf, is(new byte[]{3, 2, 1, 0}));
            reader.inputStream().close();
            assertThat(reader.next(), is(false));
        } finally {
            reader.close();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testReaderException1() throws Exception {
        RecordSet.Reader reader = recordset.newReader();
        try {
            // no current record, because next() has not been called
            reader.inputStream();
        } finally {
            reader.close();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testReaderException2() throws Exception {
        RecordSet.Writer writer = recordset.newWriter();
        try {
            writer.writeRecord();
        } finally {
            writer.close();
        }

        RecordSet.Reader reader = recordset.newReader();
        try {
            assertThat(reader.next(), is(true));
            assertThat(reader.next(), is(false));
            // no current record, because we are after last record
            reader.inputStream();
        } finally {
            reader.close();
        }
    }

    @Test
    public void testKeys() throws Exception {
        RecordSet.Key k1;
        RecordSet.Key k2;

        RecordSet.Writer writer = recordset.newWriter();
        try {
            writer.outputStream().write(new byte[] {-6, -7});
            writer.outputStream().close();
            k1 = writer.writeRecord();
            writer.outputStream().write(new byte[] {14, 15, 16, 17});
            writer.outputStream().close();
            k2 = writer.writeRecord();
        } finally {
            writer.close();
        }

        byte[] buf = new byte[5];
        InputStream r1 = recordset.getRecord(k1);
        assertThat(r1, notNullValue());
        assertThat(r1.read(buf), is(2));
        assertThat(buf, is(new byte[] {-6, -7, 0, 0, 0}));
        InputStream r2 = recordset.getRecord(k2);
        assertThat(r2, notNullValue());
        assertThat(r2.read(buf), is(4));
        assertThat(buf, is(new byte[] {14, 15, 16, 17, 0}));
    }
}
