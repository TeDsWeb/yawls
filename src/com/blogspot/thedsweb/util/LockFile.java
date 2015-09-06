/*******************************************************************************
 * Copyright (c) 2015 Dominik Brämer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.blogspot.thedsweb.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.logging.Level;

import com.blogspot.thedsweb.main.Debug;

public final class LockFile {
    private LockFile() {
    }

    public static boolean create(String path, boolean del) {
	// Generate a lock file with fileLock to prevent complications
	// if File is doesen't deleted after interruption
	final File file = new File(path);
	if (!file.exists()) {
	    try {
		file.createNewFile();
	    } catch (final IOException e) {
		Debug.LOG.log(Level.SEVERE,
			"Could not create lock file due to I/O failure.", e);
	    }
	}
	final RandomAccessFile randomAccessFile;
	try {
	    randomAccessFile = new RandomAccessFile(file, "rw");
	    final FileLock fileLock = randomAccessFile.getChannel().tryLock();
	    if (fileLock != null && del) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run() {
			try {
			    fileLock.release();
			    randomAccessFile.close();
			} catch (final IOException e1) {
			    Debug.LOG
				    .log(Level.WARNING,
					    "Could not release/close lock file due to I/O failure.",
					    e1);
			}
			file.delete();
		    }
		});
		return true;
	    }
	} catch (final FileNotFoundException e2) {
	    Debug.LOG.log(Level.SEVERE, "Could not create lock file: "
		    + Database.PATH_LOCK, e2);
	} catch (final IOException e3) {
	    Debug.LOG.log(Level.SEVERE,
		    "I/O failure while creating lock file.", e3);
	} catch (final OverlappingFileLockException e4) {
	}
	return false;
    }
}
