/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

public class Stable {
    private int savedHighestLimit;
    private int limit;
    private int brightness;
    private int count;
    private int negCount;

    public Stable(int brightness, int lowerLimit) {
	count = 0;
	negCount = 0;
	savedHighestLimit = lowerLimit;
	this.brightness = brightness;
    }

    public int getLimit() {
	return limit;
    }

    public void setStatus(int currentLimit, int lastValue) {
	limit = currentLimit;

	if (savedHighestLimit <= currentLimit) {
	    savedHighestLimit = currentLimit;
	}

	if (brightness == lastValue) {
	    count++;
	} else {
	    negCount++;
	}

	brightness = lastValue;

	if (count > 10) {
	    count = 0;
	    negCount = 0;
	    limit = savedHighestLimit;
	}
	if (negCount > 2) {
	    count = 0;
	    negCount = 0;
	    savedHighestLimit = currentLimit;
	}
    }
}
