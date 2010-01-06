package org.saturnine.api;

/**
 *
 * @author Alexey Vladykin
 */
public interface FileState {

    String getPath();

    long getSize();

    int getMode();

    long getLastModified();

    String getHash();
}
