/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.File;
import java.util.logging.Level;

import com.blogspot.thedsweb.main.Debug;

public class Battery {
    private Battery() {
    }

    public static int status() {
	final File chargeNow = new File(Database.PATH_CHARGE_NOW);
	final File chargeFull = new File(Database.PATH_CHARGE_FULL);

	// Test if files exists and return 100 if not
	if (chargeNow.exists() && chargeFull.exists()) {
	    // Read values of charge_now and charge_full
	    final int now = Files.readInt(Database.PATH_CHARGE_NOW);
	    final int full = Files.readInt(Database.PATH_CHARGE_FULL);

	    // Check if values exists return 100 if not
	    // else return battery percentage
	    if (now == 0 || full == 0) {
		Debug.LOG.log(Level.CONFIG,
			"Could not read chargeNow and chargeFull files.");
		return 100;
	    } else {
		return 100 * now / full;
	    }
	}
	Debug.LOG.log(Level.CONFIG,
		"Could not find chargeNow or chargeFull file.");
	return 100;
    }
}
