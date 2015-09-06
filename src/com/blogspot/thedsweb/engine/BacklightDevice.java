/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

import java.util.ArrayList;
import java.util.List;

public class BacklightDevice implements Backlight {
    private final int index;
    private final List<Backlight> blDevice;

    public BacklightDevice() {
	// A list of possible backlight devices sorted by priority.
	blDevice = new ArrayList<Backlight>();
	blDevice.add(new Xbrightness());
	index = 0;
    }

    @Override
    public boolean check() {
	return blDevice.get(index).check();
    }

    @Override
    public void setRef(int min, int max) {
	blDevice.get(index).setRef(min, max);
    }

    @Override
    public double[] getRef() {
	return blDevice.get(index).getRef();
    }

    @Override
    public void set(int value) {
	blDevice.get(index).set(value);
    }

    @Override
    public int get() {
	return blDevice.get(index).get();
    }
}
