package org.saturnine.util;

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

    public void update(String str) {
        checkState();
        try {
            md.update(str.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            // very unlikely
            ex.printStackTrace();
        }
    }

    public byte[] result() {
        if (result == null) {
            result = md.digest();
        }
        return result;
    }

    public String resultAsHex() {
        byte[] localResult = result();
        StringBuilder buf = new StringBuilder(2 * localResult.length);
        for (byte b : localResult) {
            buf.append(String.format("%02x", b));
        }
        return buf.toString();
    }

    private void checkState() {
        if (result != null) {
            throw new IllegalStateException("result() already called");
        }
    }
}
