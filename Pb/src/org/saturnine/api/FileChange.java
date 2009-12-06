/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.api;

/**
 *
 * @author Alexey Vladykin
 */
public interface FileChange {

    FileChangeType getType();

    FileState getOriginalState();

    FileState getResultState();
}
