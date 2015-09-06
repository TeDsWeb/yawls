/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

public class Fade {
    private int value;

    public Fade() {
	value = 0;
    }

    public void setValue(int value) {
	this.value = value;
    }

    public int getValue() {
	return value;
    }
}
