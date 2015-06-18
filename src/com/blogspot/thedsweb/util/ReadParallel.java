/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.util.concurrent.Semaphore;

public class ReadParallel implements Runnable {
    private final String PATH;
    private final Semaphore sem;
    private final int[] arr;
    private final int id;

    public ReadParallel(String path, int[] arr, int id, Semaphore sem) {
	this.PATH = path;
	this.arr = arr;
	this.id = id;
	this.sem = sem;
    }

    @Override
    public void run() {
	final int val = Files.readInt(PATH);

	try {
	    sem.acquire();
	} catch (final InterruptedException e) {
	}
	arr[id] = val;
	sem.release();
    }
}
