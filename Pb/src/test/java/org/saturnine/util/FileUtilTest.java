package org.saturnine.util;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Alexey Vladykin
 */
public class FileUtilTest {

    @Test
    public void testJoinPath() {
        assertThat(FileUtil.joinPath("", ""), is(""));
        assertThat(FileUtil.joinPath("", "a"), is("a"));
        assertThat(FileUtil.joinPath("a", ""), is("a"));
        assertThat(FileUtil.joinPath("a", "b"), is("a/b"));
    }

    @Test
    public void testNormalizePath() {
        assertThat(FileUtil.normalizePath(""), is(""));
        assertThat(FileUtil.normalizePath("a"), is("a"));
        assertThat(FileUtil.normalizePath("/a/"), is("a"));
        assertThat(FileUtil.normalizePath("./a/."), is("a"));
        assertThat(FileUtil.normalizePath("//a//b//c//"), is("a/b/c"));
    }
}
