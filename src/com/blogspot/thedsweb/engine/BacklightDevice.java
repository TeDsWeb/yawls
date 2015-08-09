package com.blogspot.thedsweb.engine;

import java.util.ArrayList;
import java.util.List;

public class BacklightDevice implements Backlight {
    private int index;
    private final List<Backlight> blDevice;

    public BacklightDevice() {
	// A list of possible backlight devices sorted by priority.
	blDevice = new ArrayList<Backlight>();
	blDevice.add(new Xbrightness());
	index = 0;
	while (index < blDevice.size() && !blDevice.get(index).check()) {
	    index++;
	}
	if (index == blDevice.size()) {
	    index = 0;
	}
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
