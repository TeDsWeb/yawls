/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.MissingResourceException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.blogspot.thedsweb.util.Config;
import com.blogspot.thedsweb.util.Database;

public class Debug {
    public final static Logger LOG = Logger.getLogger(Debug.class.getName());
    private static Handler FILEHANDLER;

    static {
	try {
	    FILEHANDLER = new FileHandler(Database.LOG_PATTERN,
		    Database.LOG_SIZE, Database.LOG_ROTATE, true);
	    LOG.addHandler(FILEHANDLER);
	} catch (final IOException e) {
	    LOG.log(Level.SEVERE, "I/O failure while creating log file.", e);
	}
	try {
	    final Config config = new Config();
	    final int level = config.logLevel();
	    if (level == 0) {
		LOG.setLevel(Level.SEVERE);
	    } else if (level == 1) {
		LOG.setLevel(Level.WARNING);
	    } else if (level == 2) {
		LOG.setLevel(Level.INFO);
	    } else {
		LOG.setLevel(Level.CONFIG);
	    }
	} catch (final MissingResourceException e1) {
	    Debug.LOG.log(Level.SEVERE, "Could not find config file.", e1);

	} catch (final MalformedURLException e2) {
	    LOG.log(Level.WARNING, "Could not read config file.", e2);
	}
    }
}
