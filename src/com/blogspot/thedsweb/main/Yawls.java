/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.main;

import java.net.MalformedURLException;
import java.util.logging.Level;

import com.blogspot.thedsweb.daemon.BrightnessDaemon;
import com.blogspot.thedsweb.engine.Brightness;
import com.blogspot.thedsweb.gui.Show;
import com.blogspot.thedsweb.util.Check;
import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Database;
import com.blogspot.thedsweb.util.Initialization;
import com.blogspot.thedsweb.util.LockFile;
import com.blogspot.thedsweb.util.Translate;

public class Yawls {
    public static void main(String[] args) {
	// Simple command line argument parsing
	for (final String options : args) {
	    switch (options) {
	    case "-h":
	    case "--help":
		help();
		break;
	    //
	    case "-r":
	    case "--restore":
		if (Check.root()) {
		    restoreConfig();
		} else {
		    rootOutput();
		}
		break;
	    //
	    case "-c":
	    case "--cron":
		if (Check.root()) {
		    try {
			if (LockFile.create(Database.PATH_CRON_LOCK, true)) {
			    if (Check.calibration()) {
				automatic();
			    }
			} else {
			    singleInstanceOutput();
			}
		    } catch (final MalformedURLException e) {
			Debug.LOG.log(Level.SEVERE,
				"Could not read config file.", e);
		    }
		} else {
		    rootOutput();
		}
		break;
	    //
	    case "-d":
	    case "--daemon":
		if (Check.root()) {
		    if (LockFile.create(Database.PATH_LOCK, true)) {
			daemon();
		    } else {
			singleInstanceOutput();
		    }
		} else {
		    rootOutput();
		}
		break;
	    //
	    case "-g":
	    case "--gui":
		if (Check.root()) {
		    if (LockFile.create(Database.PATH_GUI_LOCK, true)) {
			Show.gui(args);
		    } else {
			singleInstanceOutput();
		    }
		} else {
		    rootOutput();
		}
		break;
	    //
	    default:
		System.out.println(Translate._("Usage: yawls [OPTIONS]"));
		//
	    }
	}
	if (args == null || args.length == 0) {
	    System.out.println(Translate._("Usage: yawls [OPTIONS]"));
	}
    }

    private static void singleInstanceOutput() {
	System.out.println(Translate
		._("Another instance of yawls is already running!"));
    }

    private static void rootOutput() {
	System.out.println(Translate._("This command must be run as root"));
    }

    private static void help() {
	System.out
		.println(Translate._("Usage: yawls")
			+ "\n"
			+ Translate
				._("yawls adjusts the brightness level of your display by using\nthe internal/externel webcam of your notebook as an ambient\nlight sensor.")
			+ "\n\n"
			+ Translate
				._("If you wish to change the interval of ambient light checks\nyou need to modify the file:")
			+ "\n"
			+ "   /etc/cron.d/yawls"
			+ "\n\n"
			+ Translate._("Options:")
			+ "\n"
			+ "  "
			+ Translate
				._("--cron\t or -c use webcam as brightness reference")
			+ "\n" + "  "
			+ Translate._("--help\t or -h show this help") + "\n"
			+ "  "
			+ Translate._("--restore\t or -r restore config files")
			+ "\n");

	System.out.println(Translate._("Yawls version") + " "
		+ Database.VERSION);

	// Error handling
	final boolean blDevice = !Check.backlightDevice();
	final boolean camera = !Check.camera();
	if (blDevice || camera) {
	    System.out.println("\n" + Translate._("Error:"));
	}
	if (blDevice) {
	    System.out
		    .println("  " + Translate._("No backlight device found."));
	}
	if (camera) {
	    System.out.println("  " + Translate._("No camera found."));
	}
    }

    private static void automatic() throws MalformedURLException {
	// Initialize Files if necessary
	Initialization.Files();

	// Read configuration File
	final Config config = new Config();

	// Get current brightness and save it
	final Brightness brightness = new Brightness(config);

	// Set new brightness level
	brightness.setBrightness();
    }

    private static void daemon() {
	// Set daemon mode
	System.out.println(Translate
		._("Yawls service started. Press {CTRL+C} to abort..."));

	// Short delay to give the system time to boot
	try {
	    Thread.sleep(10000);
	} catch (final InterruptedException e) {
	}

	// Create a daemon object for brightness changes
	final BrightnessDaemon brightnessDaemon = new BrightnessDaemon();
	brightnessDaemon.start();
    }

    private static void restoreConfig() {
	// Forces initialization of saved files
	Initialization.forceFiles();
    }
}
