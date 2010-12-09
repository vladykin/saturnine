/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.saturnine.api;

/**
 *
 * @author Alexey Vladykin
 */
public class PbException extends Exception {

    public PbException(String message) {
        super(message);
    }

    public PbException(String message, Throwable cause) {
        super(message, cause);
    }
}
