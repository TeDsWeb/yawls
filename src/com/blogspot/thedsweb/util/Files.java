/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.blogspot.thedsweb.main.Debug;

public class Files {
    public Files() {
    }

    public static String readStatus(int[] arr) {
	// Read in all saved or used files and store the values in an array
	if (arr == null || arr.length < 4) {
	    throw new IllegalArgumentException();
	} else {
	    final GeneratePath brightness = new GeneratePath();
	    final String path = brightness.getBrightnessPath();
	    final Semaphore sem = new Semaphore(1, false);
	    final ExecutorService read = Executors.newCachedThreadPool();
	    read.submit(new ReadParallel(Database.PATH_MIN, arr, 0, sem));
	    read.submit(new ReadParallel(Database.PATH_MAX, arr, 1, sem));
	    read.submit(new ReadParallel(Database.PATH_LAST, arr, 2, sem));
	    read.submit(new ReadParallel(brightness.getMaxBrightnessPath(),
		    arr, 3, sem));
	    read.shutdown();
	    try {
		read.awaitTermination(10, TimeUnit.SECONDS);
	    } catch (final InterruptedException e) {
	    }
	    return path;
	}
    }

    // read int from file
    public static int readInt(String arg) {
	return Integer.parseInt(read(arg));
    }

    // read string from file
    public static String readString(String arg) {
	return read(arg);
    }

    private static String read(String arg) {
	// Buffered read out of an file
	BufferedReader br = null;
	String result = "";

	try {
	    br = new BufferedReader(new FileReader(arg));
	    result = br.readLine();
	} catch (final IOException e) {
	    Debug.LOG.log(Level.SEVERE, "I/O failure while read file: " + arg,
		    e);
	} finally {
	    try {
		if (br != null) {
		    br.close();
		}
	    } catch (final IOException ex) {
		Debug.LOG.log(Level.SEVERE,
			"I/O failure while try to close file: '" + arg
				+ "' after reading.", ex);
	    }
	}

	return result;
    }

    public static void saveValue(int current, int min, int max) {
	// Save the minimum OR maximum AND last brightness
	// value into different files
	final ExecutorService write = Executors.newCachedThreadPool();
	if (current == min) {
	    write.submit(new WriteParallel(current, Database.PATH_MIN));
	}
	if (current == max) {
	    write.submit(new WriteParallel(current, Database.PATH_MAX));
	}
	write.submit(new WriteParallel(current, Database.PATH_LAST));
	write.shutdown();
	try {
	    write.awaitTermination(10, TimeUnit.SECONDS);
	} catch (final InterruptedException e) {
	}
    }

    // write int values to file
    public static void writeInt(int x, String path) {
	final String arg = Integer.toString(x);
	write(arg, path);
    }

    // rewrite configurations file
    public static void writeConfig() {
	write(Database.CONFIG_CONTENT, Database.PATH_TO_CONFIG_FILE);
    }

    // write to file
    private static void write(String arg, String path) {
	// Buffered write into an file
	try {
	    if (path != "") {
		final File file = new File(path);

		if (!file.exists()) {
		    file.createNewFile();
		}

		final FileWriter fw = new FileWriter(file.getAbsoluteFile());
		final BufferedWriter bw = new BufferedWriter(fw);

		bw.write(arg);
		bw.close();
	    } else {
		throw new IllegalArgumentException();
	    }
	} catch (final IOException e) {
	    Debug.LOG.log(Level.SEVERE, "I/O failure while write argument "
		    + arg + " to file: " + path, e);
	}
    }

    // Try to delete the pid file of yawls
    public static void deletePid() {
	final File file = new File(Database.PATH_PID);
	file.delete();
    }
}
