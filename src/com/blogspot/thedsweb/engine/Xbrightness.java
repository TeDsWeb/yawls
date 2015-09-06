/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

import java.io.File;

import com.blogspot.thedsweb.util.Files;

public class Xbrightness extends Files implements Backlight {
    private double[] referenceValue;
    private final String path;
    private final int maxLevel;

    public Xbrightness() {
	final int[] arr = new int[4];
	path = readStatus(arr);
	maxLevel = arr[3];
    }

    @Override
    public boolean check() {
	final File file = new File(path);
	return file.exists();
    }

    @Override
    public void setRef(int min, int max) {
	// Reference mean values
	final double referenceStep = (double) (max ^ min) / (maxLevel + 1);
	final double[] referenceValue = new double[maxLevel + 1];
	referenceValue[maxLevel] = max - referenceStep;

	for (int i = maxLevel; i > 1; i--) {
	    if (referenceValue[i] - referenceStep < 0) {
		referenceValue[i - 1] = 0;
	    } else {
		referenceValue[i - 1] = referenceValue[i] - referenceStep;
	    }
	}

	this.referenceValue = referenceValue;
    }

    @Override
    public double[] getRef() {
	return referenceValue;
    }

    @Override
    public void set(int value) {
	writeInt(value, path);
    }

    @Override
    public int get() {
	return readInt(path);
    }

}
