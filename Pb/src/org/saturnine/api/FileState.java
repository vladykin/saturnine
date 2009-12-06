/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.api;

/**
 *
 * @author Alexey Vladykin
 */
public interface FileState {

    String getPath();

    long getSize();

    long getTimeModified();

    String getHash();
}
