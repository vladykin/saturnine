package org.saturnine.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class FileUtilTest {

    public FileUtilTest() {
    }

    @Test
    public void testJoinPath() {
        assertEquals("", FileUtil.joinPath("", ""));
        assertEquals("a", FileUtil.joinPath("", "a"));
        assertEquals("a", FileUtil.joinPath("a", ""));
        assertEquals("a/b", FileUtil.joinPath("a", "b"));
    }

    @Test
    public void testNormalizePath() {
        assertEquals("", FileUtil.normalizePath(""));
        assertEquals("a", FileUtil.normalizePath("a"));
        assertEquals("a", FileUtil.normalizePath("/a/"));
        assertEquals("a", FileUtil.normalizePath("./a/."));
        assertEquals("a/b/c", FileUtil.normalizePath("//a//b//c//"));
    }
}
