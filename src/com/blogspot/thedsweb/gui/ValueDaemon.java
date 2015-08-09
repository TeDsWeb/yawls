/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.gui;

import org.gnome.gtk.Label;

import com.blogspot.thedsweb.util.Files;

public class ValueDaemon implements Runnable {
    private final Label minValue;
    private final Label maxValue;
    private final Label lastValue;

    public ValueDaemon(Label minValue, Label maxValue, Label lastValue) {
	this.minValue = minValue;
	this.maxValue = maxValue;
	this.lastValue = lastValue;
    }

    @Override
    public void run() {
	final int[] arr = new int[3];
	while (true) {
	    Files.readMMLStatus(arr);
	    minValue.setLabel(Integer.toString(arr[0]));
	    maxValue.setLabel(Integer.toString(arr[1]));
	    lastValue.setLabel(Integer.toString(arr[2]));
	    try {
		Thread.sleep(1000);
	    } catch (final InterruptedException e1) {
	    }
	}
    }

}
