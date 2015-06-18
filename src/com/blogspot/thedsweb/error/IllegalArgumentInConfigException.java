/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.error;

public class IllegalArgumentInConfigException extends Exception {
    private final static long serialVersionUID = 1L;

    public IllegalArgumentInConfigException() {
    }

    public IllegalArgumentInConfigException(String message) {
	super(message);
    }
}
