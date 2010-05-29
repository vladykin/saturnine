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
            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                writeVersion(outputStream, 1);
            } finally {
                outputStream.close();
            }
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
                throw new IOException("Failed to seek to offset " + key.offset);
            }

            byte[] record = readRecord(inputStream);
            if (record == null) {
                throw new IOException("There is no record at offset " + key.offset);
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

    private static int readVersion(FileInputStream inputStream) throws IOException {
        int R = inputStream.read();
        if (R == 'R') {
            int S = inputStream.read();
            if (S == 'S') {
                int version = 0;
                int ch;
                while (0 <= (ch = inputStream.read())) {
                    if ('0' <= ch && ch <= '9') {
                        version = 10 * version + (ch - '0');
                    } else if (ch == 0) {
                        return version;
                    } else {
                        break;
                    }
                }
            }
        }
        throw new IOException("No magic");
    }

    private static void writeVersion(FileOutputStream outputStream, int version) throws IOException {
        outputStream.write(String.format("RS%d\u0000", version).getBytes("UTF-8"));
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
            readVersion(inputStream); // returned version not used yet
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

            // Initially position is zero, and is automatically moved
            // to the end of the file on first write. But we need to
            // know this position before the first write to calculate the
            // first record key. Thus this hack.
            outputStream.getChannel().position(file.length());
        }

        public OutputStream outputStream() throws IOException {
            checkState();
            if (recordOutputStream == null) {
                recordOutputStream = new ByteArrayOutputStream(1024);
            }
            return recordOutputStream;
        }

        public Key writeRecord() throws IOException {
            checkState();
            long recordKey = outputStream.getChannel().position();
            RecordSet.writeRecord(outputStream, recordOutputStream == null
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
