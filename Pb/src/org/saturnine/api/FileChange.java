package org.saturnine.api;

/**
 * @author Alexey Vladykin
 */
public interface FileChange {

    String getPath();

    FileChangeType getType();

    String getCopyOf();
}
