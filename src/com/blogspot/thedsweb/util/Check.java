/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.File;

public class Check {
    private Check() {
    }

    public static boolean root() {
	// Check if the current user has root privileges
	return System.getProperty("user.name").equalsIgnoreCase("root");
    }

    public static boolean calibration() {
	// Check if calibration is running
	final File file = new File(Database.PATH_LOCK_DAEMON);
	return !file.exists();
    }

    public static boolean configuration() {
	// Check if calibration is running
	final File file = new File(Database.PATH_LOCK_CONFIG);
	if (file.exists()) {
	    file.delete();
	    return true;
	} else {
	    return false;
	}
    }

    public static boolean camera() {
	final File file = new File("/dev/video0");
	return file.exists();
    }

    public static boolean backlightDevice() {
	final File folder = new File("/sys/class/backlight");
	final File[] listOfFiles = folder.listFiles();

	if (listOfFiles == null) {
	    return false;
	}

	return listOfFiles.length != 0;
    }
}
