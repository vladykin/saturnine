package org.saturnine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Alexey Vladykin
 */
public final class Hash {

    public static Hash createSHA1() {
        try {
            return new Hash(MessageDigest.getInstance("SHA-1"));
        } catch (NoSuchAlgorithmException ex) {
            throw new NullPointerException();
        }
    }

    private final MessageDigest md;
    private byte[] result;

    private Hash(MessageDigest md) {
        this.md = md;
    }

    public void update(byte[] buf) {
        checkState();
        md.update(buf);
    }

    public void update(byte[] buf, int len) {
        checkState();
        md.update(buf, 0, len);
    }

    public void update(String str) {
        checkState();
        try {
            md.update(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            // very unlikely
            ex.printStackTrace();
        }
    }

    public void update(CharSequence cs) {
        update(cs.toString());
    }

    public void update(long num) {
        update(String.valueOf(num));
    }

    private void update(FileInputStream inputStream) throws IOException {
        byte[] buf = new byte[1 << 14]; // 16K
        int read;
        while (0 < (read = inputStream.read(buf))) {
            update(buf, read);
        }
    }

    public void update(File file) throws IOException {
        long oldSize = file.length();
        long oldTime = file.lastModified();
        FileInputStream inputStream = new FileInputStream(file);
        try {
            update(inputStream);
        } finally {
            inputStream.close();
        }
        if (oldSize != file.length() || oldTime != file.lastModified()) {
            throw new IOException("File " + file + " modified externally");
        }
    }

    public byte[] result() {
        if (result == null) {
            result = md.digest();
        }
        return result;
    }

    public HexCharSequence resultAsHex() {
        return HexCharSequence.get(result());
    }

    private void checkState() {
        if (result != null) {
            throw new IllegalStateException("result() already called");
        }
    }
}
