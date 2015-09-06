/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.daemon;

import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import com.blogspot.thedsweb.engine.Brightness;
import com.blogspot.thedsweb.engine.Stable;
import com.blogspot.thedsweb.main.Debug;
import com.blogspot.thedsweb.util.Battery;
import com.blogspot.thedsweb.util.Check;
import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Database;
import com.blogspot.thedsweb.util.Files;
import com.blogspot.thedsweb.util.Initialization;

public class BrightnessDaemon {
    private Config config;
    private boolean calibration;
    private boolean faceDetectConfig;
    private boolean faceDetect;
    private int lowerLimit;
    private int upperLimit;
    private int[] limit;
    private Stable stable;
    private int i;
    private int before;
    private int after;
    private int minIncrease;
    private int maxIncrease;
    private int increase;
    private Brightness brightness;
    private final File pidFile;
    private final Semaphore semaphore = new Semaphore(1, false);

    public BrightnessDaemon() {
	// Initialize Files if necessary
	Initialization.Files();

	// Set PID file
	pidFile = new File(Database.PATH_PID);

	// Calibration has been running
	calibration = false;

	initialize();
    }

    private void initialize() {
	// Read configuration File and set
	// Upper limit, lower limit and face detection
	try {
	    config = new Config();
	    faceDetectConfig = config.faceDetect();
	    lowerLimit = config.lowerLimit();
	    upperLimit = config.upperLimit();
	    limit = new int[4];
	    limit[0] = lowerLimit;
	    limit[1] = upperLimit;
	    limit[2] = lowerLimit;
	    limit[3] = upperLimit;

	    // Check upper and lower limit and correct values
	    // if necessary
	    Tools.checkLimit(limit);

	    i = limit[2];

	    // Values to increase loop sleep if nothing to do
	    before = 0;
	    after = 0;
	    minIncrease = config.minIncrease();
	    maxIncrease = config.maxIncrease();
	    increase = maxIncrease;
	    stable = new Stable(after, lowerLimit);

	    // Get actual brightness
	    brightness = new Brightness(config);
	    brightness.start();
	} catch (final MalformedURLException e) {
	    Debug.LOG.log(Level.SEVERE, "Could not read config file.", e);
	}
    }

    public void start() {
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		Files.deletePid();
	    }
	});
	System.out.close();
	System.err.close();

	// Reset brightness level after reboot
	brightness.resetBrightnessAfterReboot();

	// Daemon for battery level observation
	final Thread batteryDaemon = new Thread(new BatteryDaemon(),
		"batteryDaemon");
	batteryDaemon.setDaemon(true);
	batteryDaemon.start();

	// Loop until interrupt
	while (pidFile.exists()) {
	    // Reload configuration on change
	    if (Check.configuration()) {
		brightness.interrupt();
		initialize();
	    }

	    // Check if calibration is running
	    if (Check.calibration()) {
		if (!calibration) {
		    // Get current brightness
		    brightness.setCurrent();
		} else {
		    // Reinitialize brightness after calibration
		    calibration = false;
		    brightness.interrupt();
		    brightness = new Brightness(config);
		    brightness.start();
		}
	    } else {
		calibration = true;
	    }

	    // Get last brightness value BEFORE processing
	    before = brightness.getLast();

	    /*
	     * If face detection is deactivated then set faceDetect to true only
	     * else set faceDetect to the value of the getFace function of
	     * Brightness object
	     */
	    if (faceDetectConfig) {
		faceDetect = brightness.getFace();
	    } else {
		faceDetect = true;
	    }

	    try {
		semaphore.acquire();
	    } catch (final InterruptedException e) {
	    }
	    lowerLimit = limit[2];
	    upperLimit = limit[3];
	    semaphore.release();

	    if (faceDetect) {
		// Set brightness level
		brightness.setBrightness();
		try {
		    semaphore.acquire();
		} catch (final InterruptedException e) {
		}
		upperLimit = limit[1];
		semaphore.release();
		increase = maxIncrease;
	    } else {
		// Dim display
		brightness.darkenBrightness();
		increase = minIncrease;
	    }

	    // Get last brightness value AFTER processing
	    after = brightness.getLast();

	    // Increase loop sleep
	    if (before == after) {
		if (i < upperLimit) {
		    i = i + increase;
		}
	    } else {
		i = lowerLimit;
	    }

	    // Increase correction
	    stable.setStatus(i, after);
	    i = stable.getLimit();

	    // Sleep for i seconds
	    try {
		Thread.sleep(i);
	    } catch (final InterruptedException e) {
	    }
	}
    }

    class BatteryDaemon implements Runnable {
	@Override
	public void run() {
	    try {
		semaphore.acquire();
	    } catch (final InterruptedException e) {
	    }

	    final int lowerLimitConst = limit[0];
	    final int upperLimitConst = limit[1];

	    semaphore.release();

	    int lowerLimit;
	    int j;
	    while (true) {
		// Battery friendliness
		if (Battery.status() > 30) {
		    lowerLimit = lowerLimitConst;

		    // Set sleep of daemon loop to 1 minute
		    j = 60000;
		} else {
		    lowerLimit = ((upperLimitConst - lowerLimitConst) >> 1)
			    + lowerLimitConst;

		    // Set sleep of daemon loop to 5 minutes
		    j = 300000;
		}

		try {
		    semaphore.acquire();
		} catch (final InterruptedException e) {
		}

		limit[2] = lowerLimit;

		semaphore.release();

		// Sleep for j seconds
		try {
		    Thread.sleep(j);
		} catch (final InterruptedException e) {
		}
	    }
	}
    }
}
