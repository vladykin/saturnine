package org.saturnine.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class HashTest {

    @Test
    public void testSHA1Empty() {
        Hash sha1 = Hash.createSHA1();
        assertArrayEquals(new byte[]{
                    (byte) 0xda, (byte) 0x39, (byte) 0xa3, (byte) 0xee,
                    (byte) 0x5e, (byte) 0x6b, (byte) 0x4b, (byte) 0x0d,
                    (byte) 0x32, (byte) 0x55, (byte) 0xbf, (byte) 0xef,
                    (byte) 0x95, (byte) 0x60, (byte) 0x18, (byte) 0x90,
                    (byte) 0xaf, (byte) 0xd8, (byte) 0x07, (byte) 0x09
                }, sha1.result());
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", sha1.resultAsHex().toString());
    }

    @Test
    public void testSHA1Text() {
        Hash sha1 = Hash.createSHA1();
        sha1.update("test");

        assertArrayEquals(new byte[]{
                    (byte) 0xa9, (byte) 0x4a, (byte) 0x8f, (byte) 0xe5,
                    (byte) 0xcc, (byte) 0xb1, (byte) 0x9b, (byte) 0xa6,
                    (byte) 0x1c, (byte) 0x4c, (byte) 0x08, (byte) 0x73,
                    (byte) 0xd3, (byte) 0x91, (byte) 0xe9, (byte) 0x87,
                    (byte) 0x98, (byte) 0x2f, (byte) 0xbb, (byte) 0xd3
                }, sha1.result());
        assertEquals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3", sha1.resultAsHex().toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testSHA1State() {
        Hash sha1 = Hash.createSHA1();
        sha1.update("test");
        sha1.result();
        sha1.update("test");
    }
}
