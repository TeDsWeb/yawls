/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.blogspot.thedsweb.main.Debug;

public class Config {
    private final ResourceBundle resourceBundle;

    public Config() throws MalformedURLException {
	// Load resource Bundle from configuration file path
	final File file = new File(Database.PATH_TO_CONFIG);
	final URL[] urls = { file.toURI().toURL() };
	final ClassLoader loader = new URLClassLoader(urls);
	resourceBundle = ResourceBundle.getBundle("yawls", Locale.getDefault(),
		loader);
    }

    public boolean faceDetect() {
	// Load faceDetect configuration
	return resourceBundle.getString("faceDetect").equalsIgnoreCase("true");
    }

    public int lowerLimit() {
	// Load lowerLimit configuration
	int lowerLimit = Integer.parseInt(resourceBundle
		.getString("lowerLimit"));

	// Check if value greater zero
	if (lowerLimit <= 0) {
	    // Set default value
	    lowerLimit = Database.LOWER_LIMIT;
	    Debug.LOG.log(Level.WARNING,
		    "The lower limit must be a value greater zero. Corrected to: "
			    + lowerLimit);
	}

	return lowerLimit;
    }

    public int upperLimit() {
	// Load upperLimit configuration
	int upperLimit = Integer.parseInt(resourceBundle
		.getString("upperLimit"));

	// Check if value greater zero
	if (upperLimit <= 0) {
	    // Set default value
	    upperLimit = Database.UPPER_LIMIT;
	    Debug.LOG.log(Level.WARNING,
		    "The upper limit must be a value greater zero. Corrected to: "
			    + upperLimit);
	}

	return upperLimit;
    }

    public int minIncrease() {
	// Load minIncrease configuration
	int minIncrease = Integer.parseInt(resourceBundle
		.getString("minIncrease"));

	// Check if value greater zero
	if (minIncrease <= 0) {
	    // Set default value
	    minIncrease = Database.MIN_INCREASE;
	    Debug.LOG.log(Level.WARNING,
		    "The minimum increase must be a value greater zero. Corrected to: "
			    + minIncrease);
	}

	return minIncrease;
    }

    public int maxIncrease() {
	// Load maxIncrease configuration
	int maxIncrease = Integer.parseInt(resourceBundle
		.getString("maxIncrease"));

	// Check if value greater zero
	if (maxIncrease <= 0) {
	    // Set default value
	    maxIncrease = Database.MAX_INCREASE;
	    Debug.LOG.log(Level.WARNING,
		    "The maximum increase must be a value greater zero. Corrected to: "
			    + maxIncrease);
	}

	return maxIncrease;
    }

    public int darkeningThreshold() {
	// Load darkeningThreshold configuration
	int darkeningThreshold = Integer.parseInt(resourceBundle
		.getString("darkeningThreshold"));

	// Check if value is between 0 and 100
	if (darkeningThreshold < 0 || darkeningThreshold > 100) {
	    // Set default value
	    darkeningThreshold = Database.DARKENING_THRESHOLD;
	    Debug.LOG
		    .log(Level.WARNING,
			    "The darkening threshold must be a value greater zero and smaller 100. Corrected to: "
				    + darkeningThreshold);
	}

	return darkeningThreshold;
    }

    public int brighteningThreshold() {
	// Load brighteningThreshold configuration
	int brighteningThreshold = Integer.parseInt(resourceBundle
		.getString("brighteningThreshold"));

	// Check if value is between 0 and 100
	if (brighteningThreshold < 0 || brighteningThreshold > 100) {
	    // Set default value
	    brighteningThreshold = Database.BRIGHTENING_THRESHOLD;
	    Debug.LOG
		    .log(Level.WARNING,
			    "The brightening threshold must be a value greater zero and smaller 100. Corrected to: "
				    + brighteningThreshold);
	}

	return brighteningThreshold;
    }

    public int logLevel() {
	int level = Integer.parseInt(resourceBundle.getString("logLevel"));
	if (level < 0 || level > 3) {
	    level = Database.LOG_LEVEL;
	    Debug.LOG.log(Level.WARNING,
		    "The log level must be a value greater zero and smaller 4. Corrected to: "
			    + level);
	}
	return level;
    }
}
