/*******************************************************************************
 * Copyright (c) 2013-2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.gui;

import org.gnome.gtk.Gtk;

public class Show {
    public static void gui(String[] args) {
	Gtk.init(args);
	final MainGUI mainGUI = new MainGUI();
	mainGUI.start();
	Gtk.main();
    }
}
