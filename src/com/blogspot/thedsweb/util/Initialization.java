/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.File;

public class Initialization {
    private Initialization() {
    }

    public static void Files() {
	// Initialize folder AND OR files if they dosen't exist
	createFolder();
	final File fileA = new File(Database.PATH_MIN);
	final File fileB = new File(Database.PATH_MAX);
	final File fileC = new File(Database.PATH_LAST);
	final File fileD = new File(Database.PATH_TO_CONFIG_FILE);
	if (!fileA.exists()) {
	    Files.writeInt(255, Database.PATH_MIN);
	}
	if (!fileB.exists()) {
	    Files.writeInt(0, Database.PATH_MAX);
	}
	if (!fileC.exists()) {
	    Files.writeInt(0, Database.PATH_LAST);
	}
	if (!fileD.exists()) {
	    Files.writeConfig();
	}
    }

    public static void forceFiles() {
	// Force an initialization of the "database"
	// folder and files
	createFolder();
	Files.writeInt(255, Database.PATH_MIN);
	Files.writeInt(0, Database.PATH_MAX);
	Files.writeInt(0, Database.PATH_LAST);
	Files.writeConfig();
    }

    private static void createFolder() {
	// Create the folder for "database" files
	final File dir = new File(Database.PATH_LIB);
	if (!dir.exists()) {
	    dir.mkdir();
	}
    }
}
