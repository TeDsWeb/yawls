/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.daemon;

import java.util.logging.Level;

import com.blogspot.thedsweb.main.Debug;

public class Tools {

    private Tools() {
    }

    public static void checkLimit(int[] limit) {
	final int lowerLimitConst = limit[0];
	int upperLimitConst = limit[1];
	final int lowerLimit = limit[2];
	int upperLimit = limit[3];

	// Upper limit correction
	if (lowerLimitConst > upperLimitConst) {
	    Debug.LOG
		    .log(Level.WARNING,
			    "The value of the upper limit must be greater than the one of the lower limit.");
	    upperLimitConst = lowerLimit;
	    upperLimit = lowerLimit;
	}

	// Return new upper and lower limit
	limit[1] = upperLimitConst;
	limit[2] = lowerLimit;
	limit[3] = upperLimit;
    }
}
