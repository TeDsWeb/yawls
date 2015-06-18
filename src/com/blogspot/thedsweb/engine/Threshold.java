/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.engine;

import com.blogspot.thedsweb.util.Config;

public class Threshold {
    private final double darkeningThresholdValue;
    private final double brighteningThresholdValue;

    public Threshold(Config config) {
	final double darkeningThresholdPercentage = config.darkeningThreshold();
	final double brighteningThresholdPercentage = config
		.brighteningThreshold();
	darkeningThresholdValue = (100 - darkeningThresholdPercentage) / 100;
	brighteningThresholdValue = (brighteningThresholdPercentage / 100) + 1;
    }

    public int getDarkeningThreshold(int x) {
	final double value = x * darkeningThresholdValue;
	return (int) value;
    }

    public int getBrighteningThreshold(int x) {
	final double value = x * brighteningThresholdValue;
	return (int) value;
    }
}
