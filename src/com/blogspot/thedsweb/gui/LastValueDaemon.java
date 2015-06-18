/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.gui;

import org.gnome.gtk.Label;

import com.blogspot.thedsweb.util.Database;
import com.blogspot.thedsweb.util.Files;

public class LastValueDaemon implements Runnable {
    private final Label value;

    public LastValueDaemon(Label value) {
	this.value = value;
    }

    @Override
    public void run() {
	while (true) {
	    final String v = Files.readString(Database.PATH_LAST);
	    value.setLabel(v);
	    try {
		Thread.sleep(1000);
	    } catch (final InterruptedException e) {
	    }
	}
    }

}
