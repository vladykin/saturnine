package org.saturnine.api;

import java.util.Date;

/**
 *
 * @author Alexey Vladykin
 */
public interface FileState {

    String getPath();

    long getSize();

    int getMode();

    Date getTimeModified();

    String getHash();
}
