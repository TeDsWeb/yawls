/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.gui;

import java.io.FileNotFoundException;
import java.util.logging.Level;

import org.gnome.gdk.Pixbuf;
import org.gnome.gtk.AboutDialog;
import org.gnome.gtk.License;
import org.gnome.gtk.MenuItem;
import org.gnome.gtk.MenuItem.Activate;
import org.gnome.gtk.Window;

import com.blogspot.thedsweb.main.Debug;
import com.blogspot.thedsweb.util.Database;

public class AboutGUI {
    private final MenuItem about;
    private final Window mainGUI;

    public AboutGUI(Window mainGUI, MenuItem about) {
	this.about = about;
	this.mainGUI = mainGUI;
    }

    public void start() {
	about.connect(new Activate() {
	    @Override
	    public void onActivate(MenuItem arg0) {
		mainGUI.setSensitive(false);
		final AboutDialog a = new AboutDialog();

		a.setProgramName("Yawls");

		Pixbuf icon;
		try {
		    icon = new Pixbuf(Database.PATH_ICON);
		    a.setIcon(icon);
		    a.setLogo(icon);
		} catch (final FileNotFoundException e) {
		    Debug.LOG.log(Level.WARNING, "Could not find yawls icon.");
		}

		a.setVersion(Database.VERSION);

		a.setComments("Yet Another Webcam Light Sensor");
		a.setWebsite("https://www.launchpad.net/yawls");
		a.setWebsiteLabel("launchpad.net/yawls");

		a.setLicenseType(License.GPL_3_0);

		final String[] authors = { "Dominik Brämer" };
		a.setAuthors(authors);

		a.setTranslatorCredits(Database.CREDITS);

		a.run();
		a.hide();
		mainGUI.setSensitive(true);
	    }
	});
    }
}
