/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

public class MinMaxContainer {
    private int min;
    private int max;

    public MinMaxContainer(int value) {
	min = max = value;
    }

    public int getMin() {
	return min;
    }

    public int getMax() {
	return max;
    }

    public void add(int value) {
	if (value > max) {
	    max = value;
	}
	if (value < min) {
	    min = value;
	}
    }
}
