package com.blogspot.thedsweb.engine;

import java.io.File;

import com.blogspot.thedsweb.util.Files;

public class Xbrightness extends Files implements Backlight {
    private double[] referenceValue;
    private final String PATH;
    private final int MAXLEVEL;
    private final int SUPPORTTYPE;

    public Xbrightness() {
	final int[] arr = new int[4];
	PATH = readStatus(arr);
	MAXLEVEL = arr[3];
	// TODO get supportType
	SUPPORTTYPE = 0;
    }

    @Override
    public boolean check() {
	final File file = new File(PATH);
	return file.exists();
    }

    @Override
    public void setRef(int min, int max) {
	// Reference mean values
	// TODO add better raw type handling
	final double referenceStep = (double) (max ^ min) / (MAXLEVEL + 1);
	final double[] referenceValue = new double[MAXLEVEL + 1];
	referenceValue[MAXLEVEL] = max - referenceStep;

	for (int i = MAXLEVEL; i > 1; i--) {
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
	if (SUPPORTTYPE == 0) {
	    writeInt(value, PATH);
	} else {
	    // TODO add different value handling for raw type
	}

    }

    @Override
    public int get() {
	return readInt(PATH);
    }

}
