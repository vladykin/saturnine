package org.saturnine.local;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Alexey Vladykin
 */
public final class RecordSet {

    private final File file;

    public RecordSet(File file) throws IOException {
        this.file = file;
        file.createNewFile();
    }

    public InputStream getRecord(Key key) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        try {
            if (inputStream.skip(key.offset) != key.offset) {
                throw new IOException("Key is invalid");
            }

            byte[] record = readRecord(inputStream);
            if (record == null) {
                throw new IOException("Key is invalid");
            }

            return new ByteArrayInputStream(record);
        } finally {
            inputStream.close();
        }
    }

    public Reader newReader() throws IOException {
        return new Reader();
    }

    public Writer newWriter() throws IOException {
        // TODO: lock file
        return new Writer();
    }

    private static byte[] readRecord(InputStream inputStream) throws IOException {
        byte[] buf = new byte[4];
        int bufBytesRead = inputStream.read(buf);
        if (bufBytesRead < 4) {
            return null;
        }

        int recordSize = (buf[0] << 24) | (buf[1] << 16) | (buf[2] << 8) | buf[3];
        byte[] record = new byte[recordSize];
        int recordBytesRead = inputStream.read(record);
        if (recordBytesRead < recordSize) {
            return null;
        }

        return record;
    }

    private static int writeRecord(OutputStream outputStream, byte[] record) throws IOException {
        int recordSize = record.length;
        outputStream.write(recordSize >>> 24);
        outputStream.write(recordSize >>> 16);
        outputStream.write(recordSize >>> 8);
        outputStream.write(recordSize);
        outputStream.write(record);
        return 4 + recordSize;
    }

    public static final class Key {

        private final long offset;

        public Key(long offset) {
            this.offset = offset;
        }
    }

    public final class Reader {

        private final InputStream inputStream;
        private byte[] record;
        private boolean eof;

        private Reader() throws IOException {
            this.inputStream = new FileInputStream(file);
        }

        public boolean next() throws IOException {
            if (eof) {
                return false;
            }

            record = readRecord(inputStream);
            if (record == null) {
                eof = true;
                return false;
            }

            return true;
        }

        public InputStream inputStream() throws IOException {
            if (record == null && !eof) {
                throw new IllegalStateException("Before first record");
            }
            if (eof) {
                throw new IllegalStateException("After last record");
            }

            return new ByteArrayInputStream(record);
        }

        public void close() throws IOException {
            inputStream.close();
        }
    }

    public final class Writer {

        private final OutputStream outputStream;
        private long offset;
        private ByteArrayOutputStream recordOutputStream;
        private boolean eof;

        private Writer() throws IOException {
            this.offset = file.length();
            this.outputStream = new FileOutputStream(file, true);
        }

        public Key next() throws IOException {
            writePendingRecordIfAny();
            recordOutputStream = new ByteArrayOutputStream(1024);
            return new Key(offset);
        }

        public OutputStream outputStream() throws IOException {
            if (eof) {
                throw new IllegalStateException("After last record");
            }
            if (recordOutputStream == null) {
                throw new IllegalStateException("Before first record");
            }
            return recordOutputStream;
        }

        public void close() throws IOException {
            writePendingRecordIfAny();
            eof = true;
            outputStream.close();
        }

        private void writePendingRecordIfAny() throws IOException {
            if (recordOutputStream != null) {
                offset += writeRecord(outputStream, recordOutputStream.toByteArray());
                recordOutputStream = null;
            }
        }
    }
}
