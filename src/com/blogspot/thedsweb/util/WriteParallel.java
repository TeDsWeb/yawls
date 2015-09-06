/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.util.logging.Level;

import com.blogspot.thedsweb.main.Debug;

public class WriteParallel implements Runnable {
    private final int current;
    private final String path;

    public WriteParallel(int current, String path) {
	this.current = current;
	this.path = path;
    }

    @Override
    public void run() {
	Files.writeInt(current, path);
	Debug.LOG.log(Level.CONFIG, "Save " + current + " in file " + path);
    }
}
