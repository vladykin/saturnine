package org.saturnine.util;

import java.io.File;
import java.io.InputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class RecordSetTest {

    private File recordsetFile;
    private RecordSet recordset;

    @Before
    public void setUp() throws Exception {
        recordsetFile = new File("build/test/recordset");
        recordset = RecordSet.create(recordsetFile);
    }

    @After
    public void tearDown() throws Exception {
        recordsetFile.delete();
    }

    @Test
    public void testReaderNext() throws Exception {
        RecordSet.Reader reader = recordset.newReader();
        try {
            assertNotNull(reader);
            assertFalse(reader.next());
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
            assertTrue(reader.next());
            assertFalse(reader.next());
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
            assertTrue(reader.next());
            assertEquals(3, reader.inputStream().read(buf));
            assertArrayEquals(new byte[]{1, 2, 3, 0}, buf);
            reader.inputStream().close();
            assertTrue(reader.next());
            assertEquals(3, reader.inputStream().read(buf));
            assertArrayEquals(new byte[]{3, 2, 1, 0}, buf);
            reader.inputStream().close();
            assertFalse(reader.next());
        } finally {
            reader.close();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testReaderException1() throws Exception {
        RecordSet.Reader reader = recordset.newReader();
        try {
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
            assertTrue(reader.next());
            assertFalse(reader.next());
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
        assertNotNull(r1);
        assertEquals(2, r1.read(buf));
        assertArrayEquals(new byte[] {-6, -7, 0, 0, 0}, buf);
        InputStream r2 = recordset.getRecord(k2);
        assertNotNull(r2);
        assertEquals(4, r2.read(buf));
        assertArrayEquals(new byte[] {14, 15, 16, 17, 0}, buf);
    }
}
