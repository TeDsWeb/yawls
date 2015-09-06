/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.File;
import java.util.logging.Level;

import com.blogspot.thedsweb.error.NoBacklightDeviceFoundException;
import com.blogspot.thedsweb.main.Debug;

public class GeneratePath {
    private String max_brightness;
    private String brightness;
    private String bestTypeFolder;
    private String type;

    public GeneratePath() {
	final File folder = new File("/sys/class/backlight");
	final File[] listOfFiles = folder.listFiles();
	if (listOfFiles == null || listOfFiles.length == 0) {
	    try {
		throw new NoBacklightDeviceFoundException();
	    } catch (final NoBacklightDeviceFoundException e) {
		Debug.LOG.log(Level.SEVERE, "No backlight device found.", e);
	    }
	}

	// Chose best sysfs control files by type firmware > platform > raw
	bestTypeFolder = bestChoice(listOfFiles);
	Debug.LOG.log(Level.CONFIG,
		"Backlight device path: /sys/class/backlight" + bestTypeFolder + "/");

	max_brightness = classBrightness("max_brightness");
	brightness = classBrightness("brightness");
    }

    public GeneratePath(boolean typeOnly) {
	final File folder = new File("/sys/class/backlight");
	final File[] listOfFiles = folder.listFiles();
	if (listOfFiles == null || listOfFiles.length == 0) {
	    try {
		throw new NoBacklightDeviceFoundException();
	    } catch (final NoBacklightDeviceFoundException e) {
		Debug.LOG.log(Level.SEVERE, "No backlight device found.", e);
	    }
	}

	bestChoice(listOfFiles);
    }

    public String getBrightnessPath() {
	return brightness;
    }

    public String getMaxBrightnessPath() {
	return max_brightness;
    }

    public String getType() {
	return type;
    }

    private String classBrightness(String arg) {
	// Try to get the correct path for main display max_brightness control
	// file
	return "/sys/class/backlight/" + bestTypeFolder + "/" + arg;
    }

    // Sysfs test for best type of control
    private String bestChoice(File[] listOfFiles) {
	final String[] types = { "firmware", "platform", "raw" };
	String check;
	for (final String type : types) {
	    for (int i = 0; i < listOfFiles.length; i++) {
		check = Files.readString("/sys/class/backlight/"
			+ listOfFiles[i].getName() + "/type");
		if (type.equalsIgnoreCase(check)) {
		    this.type = type;
		    return listOfFiles[i].getName();
		}
	    }
	}
	if (listOfFiles.length > 0) {
	    return listOfFiles[0].getName();
	} else {
	    return "";
	}
    }
}
