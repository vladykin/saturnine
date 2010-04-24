package org.saturnine.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
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

    public static RecordSet create(File file) throws IOException {
        return new RecordSet(file, true);
    }

    public static RecordSet open(File file) throws IOException {
        return new RecordSet(file, false);
    }

    private final File file;

    private RecordSet(File file, boolean create) throws IOException {
        if (create) {
            new FileOutputStream(file).close();
        }
        if (!file.exists()) {
            throw new IOException(file.getPath() + " does not exist");
        }
        this.file = file;
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

    private static int readInt(InputStream inputStream) throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        int b4 = inputStream.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        }
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    private static void writeInt(FileOutputStream outputStream, int n) throws IOException {
        outputStream.write(n >>> 24);
        outputStream.write(n >>> 16);
        outputStream.write(n >>> 8);
        outputStream.write(n);
    }

    private static byte[] readRecord(FileInputStream inputStream) throws IOException {
        if (inputStream.getChannel().size() - inputStream.getChannel().position() < 4) {
            return null;
        }
        int recordSize = readInt(inputStream);
        byte[] record = new byte[recordSize];
        int recordBytesRead = inputStream.read(record);
        if (recordBytesRead < recordSize) {
            return null;
        }

        return record;
    }

    private static void writeRecord(FileOutputStream outputStream, byte[] record) throws IOException {
        writeInt(outputStream, record.length);
        outputStream.write(record);
    }

    public static final class Key {

        private final long offset;

        public Key(long offset) {
            this.offset = offset;
        }
    }

    public final class Reader {

        private final FileInputStream inputStream;
        private long recordKey;
        private byte[] record;
        private boolean eof;

        private Reader() throws IOException {
            this.inputStream = new FileInputStream(file);
        }

        public boolean next() throws IOException {
            if (eof) {
                return false;
            }

            recordKey = inputStream.getChannel().position();
            record = readRecord(inputStream);
            if (record == null) {
                eof = true;
                return false;
            }

            return true;
        }

        public Key key() throws IOException {
            checkState();
            return new Key(recordKey);
        }

        public InputStream inputStream() throws IOException {
            checkState();
            return new ByteArrayInputStream(record);
        }

        public void close() throws IOException {
            inputStream.close();
        }

        private void checkState() {
            if (record == null && !eof) {
                throw new IllegalStateException("Before first record");
            }
            if (eof) {
                throw new IllegalStateException("After last record");
            }
        }
    }

    public final class Writer {

        private final FileOutputStream outputStream;
        private ByteArrayOutputStream recordOutputStream;
        private boolean eof;

        private Writer() throws IOException {
            this.outputStream = new FileOutputStream(file, true);
        }

        public OutputStream outputStream() throws IOException {
            checkState();
            if (recordOutputStream == null) {
                recordOutputStream = new ByteArrayOutputStream(1024);
            }
            return recordOutputStream;
        }

        public Key closeRecord() throws IOException {
            checkState();
            long recordKey = outputStream.getChannel().position();
            writeRecord(outputStream, recordOutputStream == null
                    ? new byte[0] : recordOutputStream.toByteArray());
            recordOutputStream = null;
            return new Key(recordKey);
        }

        public void close() throws IOException {
            eof = true;
            outputStream.close();
        }

        private void checkState() {
            if (eof) {
                throw new IllegalStateException("Writer is already closed");
            }
        }
    }
}
