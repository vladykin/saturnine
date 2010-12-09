package org.saturnine.util;

/**
 * @author Alexey Vladykin
 */
public final class HexCharSequence implements CharSequence {

    private final byte[] data;
    private final int start;
    private final int end;

    public static HexCharSequence get(CharSequence cs) {
        if (cs instanceof HexCharSequence) {
            return (HexCharSequence) cs;
        }
        byte[] data = new byte[(cs.length() + 1) / 2];
        for (int i = 0; i < cs.length(); ++i) {
            int digit = Character.digit(cs.charAt(i), 16);
            if (digit == -1) {
                throw new IllegalArgumentException("Illegal character: " + cs.charAt(i));
            }
            int shift = 4 * (1 - i % 2);
            data[i / 2] |= digit << shift;
        }
        return new HexCharSequence(data, 0, cs.length());
    }

    public static HexCharSequence get(byte[] data) {
        return new HexCharSequence(data, 0, 2 * data.length);
    }

    public static HexCharSequence get(byte[] data, int start, int end) {
        return new HexCharSequence(data, start, end);
    }

    private HexCharSequence(byte[] data, int start, int end) {
        if (start < 0 || 2 * data.length < start) {
            throw new IllegalArgumentException();
        }
        if (end < start || 2 * data.length < end) {
            throw new IllegalArgumentException();
        }
        this.data = data.clone();
        this.start = start;
        this.end = end;
    }

    @Override
    public int length() {
        return end - start;
    }

    public int byteLength() {
        return (length() + 1) / 2;
    }

    @Override
    public char charAt(int index) {
        byte b = data[(start + index) / 2];
        int shift = (1 - (start + index) % 2) * 4;
        return Character.forDigit((b >>> shift) & 0xF, 16);
    }

    public byte byteAt(int index) {
        return data[start + index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new HexCharSequence(data, this.start + start, this.start + end);
    }

    @Override
    public String toString() {
        return new StringBuilder(this).toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HexCharSequence)) {
            return false;
        }
        final HexCharSequence that = (HexCharSequence) obj;
        if (this.length() != that.length()) {
            return false;
        }
        for (int i = 0; i < this.length(); ++i) {
            if (this.charAt(i) != that.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (int i = 0; i < length(); ++i) {
            hash = 13 * hash + charAt(i);
        }
        return hash;
    }
}
